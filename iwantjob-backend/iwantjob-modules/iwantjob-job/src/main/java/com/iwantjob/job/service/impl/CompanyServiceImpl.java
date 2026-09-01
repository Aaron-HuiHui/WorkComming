package com.iwantjob.job.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.iwantjob.common.exception.BusinessException;
import com.iwantjob.common.result.ErrorCode;
import com.iwantjob.job.dto.CompanyUpdateDTO;
import com.iwantjob.job.dto.CompanyVO;
import com.iwantjob.job.entity.Company;
import com.iwantjob.job.entity.Job;
import com.iwantjob.job.mapper.CompanyMapper;
import com.iwantjob.job.mapper.JobMapper;
import com.iwantjob.job.service.CompanyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 企业信息服务实现
 * 认领规则：HR 须发布过该企业名称的职位（或管理员），且企业未被他人认领
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {

    private final CompanyMapper companyMapper;
    private final JobMapper jobMapper;

    @Override
    public List<CompanyVO> listCompanies(String industry) {
        List<Company> list = companyMapper.selectList(
                new LambdaQueryWrapper<Company>()
                        .eq(industry != null && !industry.isBlank(), Company::getIndustry, industry)
                        .orderByAsc(Company::getId));
        return list.stream().map(this::toVO).toList();
    }

    @Override
    public CompanyVO getCompany(Long id) {
        Company company = companyMapper.selectById(id);
        if (company == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        return toVO(company);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCompany(Long userId, Integer role, Long companyId, CompanyUpdateDTO dto) {
        Company company = companyMapper.selectById(companyId);
        if (company == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        // 管理员放行；HR 须发布过该企业的职位
        if (role == null || role != 9) {
            Long published = jobMapper.selectCount(
                    new LambdaQueryWrapper<Job>()
                            .eq(Job::getPosterId, userId)
                            .eq(Job::getCompanyName, company.getName()));
            if (published == null || published == 0) {
                throw new BusinessException(ErrorCode.FORBIDDEN);
            }
        }
        // 已被他人认领则拒绝
        if (company.getClaimedBy() != null && !company.getClaimedBy().equals(userId) && (role == null || role != 9)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        Company upd = new Company();
        upd.setId(companyId);
        upd.setIndustry(dto.getIndustry());
        upd.setScale(dto.getScale());
        upd.setHeadquarters(dto.getHeadquarters());
        upd.setLogo(dto.getLogo());
        upd.setIntro(dto.getIntro());
        upd.setCulture(dto.getCulture());
        upd.setWelfare(dto.getWelfare());
        upd.setWebsite(dto.getWebsite());
        if (company.getClaimedBy() == null) {
            upd.setClaimedBy(userId);
        }
        companyMapper.updateById(upd);
        log.info("企业主页更新: companyId={}, userId={}, name={}", companyId, userId, company.getName());
    }

    private CompanyVO toVO(Company c) {
        CompanyVO vo = new CompanyVO();
        org.springframework.beans.BeanUtils.copyProperties(c, vo);
        vo.setJobCount(jobMapper.countJobsByCompanyId(c.getId()));
        return vo;
    }
}