package com.iwantjob.salary.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iwantjob.common.exception.BusinessException;
import com.iwantjob.common.result.ErrorCode;
import com.iwantjob.salary.dto.WhitepaperVO;
import com.iwantjob.salary.entity.SalaryReportData;
import com.iwantjob.salary.entity.SalaryWhitepaper;
import com.iwantjob.salary.enums.SalaryAccessLevelEnum;
import com.iwantjob.salary.enums.SalaryVerifiedEnum;
import com.iwantjob.salary.mapper.SalaryReportDataMapper;
import com.iwantjob.salary.mapper.SalaryWhitepaperMapper;
import com.iwantjob.salary.service.SalaryWhitepaperService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 薪资白皮书服务实现
 * 关键点：
 * 1. 聚合已审核数据，按城市/岗位/学历/行业分组计算 P25/P50/P75/P99
 * 2. 分位值用 Java 排序取百分位（线性插值法）
 * 3. 简版公开，高级章节需贡献记录 ≥1
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SalaryWhitepaperServiceImpl implements SalaryWhitepaperService {

    private final SalaryReportDataMapper salaryReportDataMapper;
    private final SalaryWhitepaperMapper salaryWhitepaperMapper;
    private final ObjectMapper objectMapper;

    /** 版本号格式：yyyy-MM */
    private static final DateTimeFormatter VERSION_FMT = DateTimeFormatter.ofPattern("yyyy-MM");

    /** 分位值列表 */
    private static final double[] PERCENTILES = {25, 50, 75, 99};

    @Override
    public WhitepaperVO getLatest(Long userId) {
        SalaryWhitepaper wp = salaryWhitepaperMapper.selectOne(
                new LambdaQueryWrapper<SalaryWhitepaper>()
                        .orderByDesc(SalaryWhitepaper::getGeneratedAt)
                        .last("LIMIT 1"));
        if (wp == null) {
            throw new BusinessException(ErrorCode.WHITEPAPER_NOT_FOUND, "暂无白皮书");
        }
        return toVO(wp, userId);
    }

    @Override
    public WhitepaperVO getById(Long userId, Long id) {
        SalaryWhitepaper wp = salaryWhitepaperMapper.selectById(id);
        if (wp == null) {
            throw new BusinessException(ErrorCode.WHITEPAPER_NOT_FOUND);
        }
        return toVO(wp, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long generateWhitepaper() {
        // 1. 查询所有已审核通过的薪资数据
        List<SalaryReportData> approvedList = salaryReportDataMapper.selectList(
                new LambdaQueryWrapper<SalaryReportData>()
                        .eq(SalaryReportData::getVerified, SalaryVerifiedEnum.APPROVED.getCode()));

        log.info("开始生成白皮书，已审核数据量: {}", approvedList.size());

        // 2. 计算薪资均值列表（用于整体统计）
        List<Integer> overallSalaries = approvedList.stream()
                .filter(d -> d.getSalaryMin() != null && d.getSalaryMax() != null)
                .map(d -> (d.getSalaryMin() + d.getSalaryMax()) / 2)
                .collect(Collectors.toList());

        // 3. 按 城市/岗位/学历/行业 分组
        Map<String, List<Integer>> groupMap = new HashMap<>();
        for (SalaryReportData d : approvedList) {
            if (d.getSalaryMin() == null || d.getSalaryMax() == null) continue;
            String key = buildGroupKey(d.getCity(), d.getPosition(),
                    d.getEducationLevel(), d.getIndustry());
            groupMap.computeIfAbsent(key, k -> new ArrayList<>())
                    .add((d.getSalaryMin() + d.getSalaryMax()) / 2);
        }

        // 4. 构建分组统计结果
        List<Map<String, Object>> groupStats = new ArrayList<>();
        for (Map.Entry<String, List<Integer>> entry : groupMap.entrySet()) {
            List<Integer> salaries = entry.getValue();
            if (salaries.isEmpty()) continue;

            Map<String, Object> stat = new HashMap<>();
            String[] parts = entry.getKey().split("\\|", -1);
            stat.put("city", parts[0]);
            stat.put("position", parts[1]);
            stat.put("educationLevel", parseInteger(parts[2]));
            stat.put("industry", parts[3]);
            stat.put("sampleCount", salaries.size());
            putPercentiles(stat, salaries);
            groupStats.add(stat);
        }

        // 5. 构建整体统计
        Map<String, Object> overall = new HashMap<>();
        overall.put("sampleCount", overallSalaries.size());
        if (!overallSalaries.isEmpty()) {
            putPercentiles(overall, overallSalaries);
        }

        // 6. 构建 report JSON
        String version = LocalDateTime.now().format(VERSION_FMT);
        Map<String, Object> report = new HashMap<>();
        report.put("version", version);
        report.put("generatedAt", LocalDateTime.now().toString());
        report.put("totalSamples", overallSalaries.size());
        report.put("cityCount", (int) approvedList.stream()
                .map(SalaryReportData::getCity)
                .filter(c -> c != null && !c.isBlank())
                .distinct()
                .count());
        report.put("positionCount", (int) approvedList.stream()
                .map(SalaryReportData::getPosition)
                .filter(p -> p != null && !p.isBlank())
                .distinct()
                .count());
        report.put("overall", overall);
        report.put("groups", groupStats);

        String reportJson;
        try {
            reportJson = objectMapper.writeValueAsString(report);
        } catch (JsonProcessingException e) {
            log.error("白皮书 JSON 序列化失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "白皮书生成失败");
        }

        // 7. 保存白皮书
        SalaryWhitepaper wp = new SalaryWhitepaper();
        wp.setVersion(version);
        wp.setTitle("大学生就业薪酬白皮书 - " + version);
        wp.setReportJson(reportJson);
        wp.setGeneratedAt(LocalDateTime.now());
        wp.setAccessLevel(SalaryAccessLevelEnum.CONTRIBUTOR.getCode());
        salaryWhitepaperMapper.insert(wp);

        log.info("白皮书生成成功: id={}, version={}, samples={}, groups={}",
                wp.getId(), version, overallSalaries.size(), groupStats.size());
        return wp.getId();
    }

    /**
     * 实体转 VO，根据用户贡献记录判断是否解锁高级章节
     */
    private WhitepaperVO toVO(SalaryWhitepaper wp, Long userId) {
        WhitepaperVO vo = new WhitepaperVO();
        vo.setId(wp.getId());
        vo.setVersion(wp.getVersion());
        vo.setTitle(wp.getTitle());
        vo.setGeneratedAt(wp.getGeneratedAt());
        vo.setAccessLevel(wp.getAccessLevel());

        // 判断是否解锁高级章节：
        // - access_level=0（公开）直接解锁
        // - access_level=1（贡献者）需有 ≥1 条审核通过的贡献
        boolean unlocked = wp.getAccessLevel() == null
                || wp.getAccessLevel() == SalaryAccessLevelEnum.PUBLIC.getCode();
        if (!unlocked && userId != null) {
            int approvedCount = salaryReportDataMapper.countApprovedByUser(userId);
            unlocked = approvedCount >= 1;
        }
        vo.setAdvancedUnlocked(unlocked);
        // 仅解锁时返回完整 report_json
        if (unlocked) {
            vo.setReportJson(wp.getReportJson());
        }
        return vo;
    }

    /**
     * 构建分组 key: city|position|educationLevel|industry
     */
    private String buildGroupKey(String city, String position,
                                 Integer educationLevel, String industry) {
        return nullToEmpty(city) + "|"
                + nullToEmpty(position) + "|"
                + (educationLevel == null ? "" : educationLevel) + "|"
                + nullToEmpty(industry);
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private Integer parseInteger(String s) {
        if (s == null || s.isEmpty()) return null;
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 计算并填充 P25/P50/P75/P99 到统计 Map
     */
    private void putPercentiles(Map<String, Object> stat, List<Integer> salaries) {
        List<Integer> sorted = new ArrayList<>(salaries);
        java.util.Collections.sort(sorted);
        stat.put("p25", percentile(sorted, 25));
        stat.put("p50", percentile(sorted, 50));
        stat.put("p75", percentile(sorted, 75));
        stat.put("p99", percentile(sorted, 99));
    }

    /**
     * 百分位计算（线性插值法）
     * 排序后按位置 (p/100)*(n-1) 取值，非整数位置用线性插值
     *
     * @param sorted 已升序排序的列表
     * @param p      百分位（0~100）
     * @return 百分位值
     */
    private int percentile(List<Integer> sorted, double p) {
        if (sorted.isEmpty()) return 0;
        if (sorted.size() == 1) return sorted.get(0);
        double pos = (p / 100.0) * (sorted.size() - 1);
        int lower = (int) Math.floor(pos);
        int upper = (int) Math.ceil(pos);
        if (lower == upper) return sorted.get(lower);
        double fraction = pos - lower;
        return (int) Math.round(sorted.get(lower) + fraction * (sorted.get(upper) - sorted.get(lower)));
    }
}
