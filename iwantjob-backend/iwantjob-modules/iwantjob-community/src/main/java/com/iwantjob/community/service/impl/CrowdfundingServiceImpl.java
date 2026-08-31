package com.iwantjob.community.service.impl;

import com.iwantjob.common.exception.BusinessException;
import com.iwantjob.common.result.ErrorCode;
import com.iwantjob.community.dto.CrowdfundingCreateDTO;
import com.iwantjob.community.dto.CrowdfundingSupportDTO;
import com.iwantjob.community.dto.CrowdfundingVO;
import com.iwantjob.community.entity.CrowdfundingProject;
import com.iwantjob.community.enums.CrowdfundingStatusEnum;
import com.iwantjob.community.mapper.CrowdfundingProjectMapper;
import com.iwantjob.community.service.CrowdfundingService;
import com.iwantjob.framework.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 众筹服务实现
 */
@Service
@RequiredArgsConstructor
public class CrowdfundingServiceImpl implements CrowdfundingService {

    private final CrowdfundingProjectMapper crowdfundingProjectMapper;

    @Override
    @Transactional
    public CrowdfundingVO createCrowdfunding(CrowdfundingCreateDTO dto) {
        Long userId = SecurityUtils.requireCurrentUserId();
        CrowdfundingProject project = new CrowdfundingProject();
        project.setInitiatorId(userId);
        project.setTitle(dto.getTitle());
        project.setDescription(dto.getDescription());
        project.setGoalAmount(dto.getGoalAmount());
        project.setCurrentAmount(BigDecimal.ZERO);
        project.setStatus(CrowdfundingStatusEnum.ACTIVE.getCode());
        project.setEndDate(dto.getEndDate());
        crowdfundingProjectMapper.insert(project);
        return toVO(project);
    }

    @Override
    @Transactional
    public CrowdfundingVO supportCrowdfunding(Long id, CrowdfundingSupportDTO dto) {
        // 校验登录态（接口要求已登录）
        SecurityUtils.requireCurrentUserId();
        CrowdfundingProject project = crowdfundingProjectMapper.selectById(id);
        if (project == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "众筹项目不存在");
        }
        if (project.getStatus() == null
                || project.getStatus() != CrowdfundingStatusEnum.ACTIVE.getCode()) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "众筹项目已结束");
        }
        if (project.getEndDate() != null && project.getEndDate().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "众筹已截止");
        }
        // 原子自增当前金额
        int rows = crowdfundingProjectMapper.incrementCurrentAmount(id, dto.getAmount());
        if (rows == 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED);
        }
        // 重新查询最新金额并判断是否达成目标
        project = crowdfundingProjectMapper.selectById(id);
        if (project.getCurrentAmount().compareTo(project.getGoalAmount()) >= 0) {
            project.setStatus(CrowdfundingStatusEnum.SUCCESS.getCode());
            crowdfundingProjectMapper.updateById(project);
        }
        return toVO(project);
    }

    private CrowdfundingVO toVO(CrowdfundingProject project) {
        CrowdfundingVO vo = new CrowdfundingVO();
        BeanUtils.copyProperties(project, vo);
        return vo;
    }
}
