package com.iwantjob.job.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iwantjob.common.exception.BusinessException;
import com.iwantjob.common.result.ErrorCode;
import com.iwantjob.common.result.PageResult;
import com.iwantjob.job.dto.HrJobVO;
import com.iwantjob.job.dto.JobCreateDTO;
import com.iwantjob.job.dto.JobSearchDTO;
import com.iwantjob.job.dto.JobStatsVO;
import com.iwantjob.job.dto.JobVO;
import com.iwantjob.job.dto.NameValueVO;
import com.iwantjob.job.entity.Job;
import com.iwantjob.job.mapper.JobMapper;
import com.iwantjob.job.es.JobSearchEngine;
import com.iwantjob.job.service.JobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 职位服务实现
 * <p>
 * 搜索路由：有关键词且存在 JobSearchEngine 实现（职位服务部署了 ES）→ 相关度检索后按序回表；
 * 否则回退 MySQL FULLTEXT（MATCH...AGAINST），核心服务无 ES 依赖仍可独立运行。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {

    private final JobMapper jobMapper;
    private final ObjectProvider<JobSearchEngine> searchEngineProvider;

    @Override
    public PageResult<JobVO> searchJobs(JobSearchDTO param) {
        String keyword = param.getKeyword();
        if (keyword != null && !keyword.isBlank()) {
            JobSearchEngine engine = searchEngineProvider.getIfAvailable();
            if (engine != null) {
                try {
                    JobSearchEngine.EsHitPage hit = engine.searchIds(param);
                    return pageByIds(hit.ids(), hit.total(), param.getPage(), param.getSize());
                } catch (Exception e) {
                    log.warn("ES 职位检索失败，回退 MySQL FULLTEXT: keyword={}, err={}", keyword, e.getMessage());
                }
            }
        }
        return searchByMysql(param);
    }

    /**
     * ES 命中 ID → 按相关度顺序批量回表（保证结果顺序与得分一致）
     */
    private PageResult<JobVO> pageByIds(List<Long> ids, long total, long page, long size) {
        if (ids == null || ids.isEmpty()) {
            return PageResult.of(List.of(), total, page, size);
        }
        Map<Long, Job> byId = jobMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(Job::getId, Function.identity()));
        List<JobVO> vos = ids.stream()
                .map(byId::get)
                .filter(java.util.Objects::nonNull)
                .map(this::toVO)
                .toList();
        return PageResult.of(vos, total, page, size);
    }

    private PageResult<JobVO> searchByMysql(JobSearchDTO param) {
        Page<Job> page = new Page<>(param.getPage(), param.getSize());
        IPage<Job> result = jobMapper.searchJobs(page, param.getKeyword(), param.getType(), param.getCity(), param.getBatch(), param.getCompanyId());

        List<JobVO> vos = result.getRecords().stream()
                .map(this::toVO)
                .toList();
        return PageResult.of(vos, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public JobVO getJobDetail(Long id) {
        Job job = jobMapper.selectById(id);
        if (job == null) {
            throw new BusinessException(ErrorCode.JOB_NOT_FOUND);
        }
        // 浏览数自增（乐观更新，不影响详情返回）
        jobMapper.incrementViewCount(id);
        return toVO(job);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long publishJob(Long posterId, JobCreateDTO dto) {
        Job job = new Job();
        BeanUtils.copyProperties(dto, job);
        job.setPosterId(posterId);
        job.setSource(0);   // 0-用户发布
        job.setStatus(1);   // 1-正常
        job.setViewCount(0);
        jobMapper.insert(job);
        log.info("发布职位成功: id={}, posterId={}, title={}", job.getId(), posterId, job.getTitle());
        syncToEngineAfterCommit(job);
        return job.getId();
    }

    /**
     * 事务提交后再同步索引，避免事务回滚后 ES 残留脏文档；同步失败仅记日志
     */
    private void syncToEngineAfterCommit(Job job) {
        JobSearchEngine engine = searchEngineProvider.getIfAvailable();
        if (engine == null) {
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    engine.upsert(job);
                } catch (Exception e) {
                    log.warn("ES 索引同步失败(发布职位): jobId={}, err={}", job.getId(), e.getMessage());
                }
            }
        });
    }

    @Override
    public PageResult<HrJobVO> getMyPublishedJobs(Long posterId, long page, long size) {
        Page<HrJobVO> p = new Page<>(page, size);
        IPage<HrJobVO> result = jobMapper.selectMyPublished(p, posterId);
        return PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public JobStatsVO getStatsOverview() {
        JobStatsVO vo = new JobStatsVO();
        vo.setTotalJobs(jobMapper.statsTotalJobs());
        // 招聘批次分布
        List<NameValueVO> batchDist = jobMapper.statsByBatch();
        vo.setBatchDist(batchDist.stream().peek(nv -> nv.setName(batchLabel(nv.getName()))).toList());
        vo.setTotalCompanies(jobMapper.statsTotalCompanies());
        // 城市分布（SQL 直出）
        List<NameValueVO> cityDist = jobMapper.statsByCity();
        vo.setCityDist(cityDist.stream().peek(nv -> nv.setName(cityLabel(nv.getName()))).toList());
        // 职位类型分布（0/1/2 → 实习/校招/社招）
        List<NameValueVO> typeDist = jobMapper.statsByType();
        vo.setTypeDist(typeDist.stream().peek(nv -> nv.setName(typeLabel(nv.getName()))).toList());
        // 薪资段分布（Java 归类）
        vo.setSalaryDist(classifySalary(jobMapper.statsBySalaryRaw()));
        // 热门职位
        vo.setHotJobs(jobMapper.statsHotJobs(10));
        return vo;
    }

    /**
     * 薪资段归类：实习日薪（含"/天"） / 10k以下 / 10-20k / 20-30k / 30k以上
     * 解析 salary_range 首个数字作为月薪下限（如 "20k-35k" → 20）
     */
    private List<NameValueVO> classifySalary(List<NameValueVO> raw) {
        Map<String, Long> buckets = new TreeMap<>();
        String[] order = {"实习日薪", "10k以下", "10k-20k", "20k-30k", "30k以上"};
        for (String k : order) {
            buckets.put(k, 0L);
        }
        for (NameValueVO nv : raw) {
            String s = nv.getName() == null ? "" : nv.getName().trim();
            Long cnt = nv.getValue() == null ? 0L : nv.getValue();
            if (s.contains("/天") || s.contains("/日")) {
                buckets.merge("实习日薪", cnt, Long::sum);
                continue;
            }
            Integer lower = parseLowerK(s);
            if (lower == null) {
                continue;
            }
            if (lower < 10) {
                buckets.merge("10k以下", cnt, Long::sum);
            } else if (lower < 20) {
                buckets.merge("10k-20k", cnt, Long::sum);
            } else if (lower < 30) {
                buckets.merge("20k-30k", cnt, Long::sum);
            } else {
                buckets.merge("30k以上", cnt, Long::sum);
            }
        }
        List<NameValueVO> result = new ArrayList<>();
        for (String k : order) {
            result.add(new NameValueVO(k, buckets.get(k)));
        }
        return result;
    }

    /**
     * 解析薪资下限（k 为单位的数字），失败返回 null
     */
    private Integer parseLowerK(String s) {
        // 去掉空格，匹配首个数字（支持 "20k-35k" / "20K-35K" / "15k以上")
        String t = s.replace(" ", "").toLowerCase();
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d+(?:\\.\\d+)?)").matcher(t);
        if (!m.find()) {
            return null;
        }
        try {
            double v = Double.parseDouble(m.group(1));
            // 纯数字且小于1000视为k单位（如 "20"），大于等于1000视为元/月需换算
            if (v >= 1000) {
                return (int) Math.round(v / 1000.0);
            }
            return (int) Math.round(v);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String batchLabel(String raw) {
        if (raw == null) {
            return "日常";
        }
        return switch (raw.trim()) {
            case "0" -> "日常招聘";
            case "1" -> "春季校园招聘";
            case "2" -> "秋季校园招聘";
            case "3" -> "实习生专项批";
            default -> "日常招聘";
        };
    }

    private String cityLabel(String raw) {
        return raw == null || raw.isBlank() ? "其他/远程" : raw.trim();
    }

    private String typeLabel(String raw) {
        if (raw == null) {
            return "其他";
        }
        return switch (raw.trim()) {
            case "0" -> "实习";
            case "1" -> "校招";
            case "2" -> "社招";
            default -> "其他";
        };
    }

    private JobVO toVO(Job job) {
        JobVO vo = new JobVO();
        BeanUtils.copyProperties(job, vo);
        return vo;
    }
}