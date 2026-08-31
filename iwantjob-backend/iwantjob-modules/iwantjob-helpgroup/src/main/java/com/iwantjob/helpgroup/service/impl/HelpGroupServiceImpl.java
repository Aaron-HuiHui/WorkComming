package com.iwantjob.helpgroup.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iwantjob.common.enums.BadgeCondEnum;
import com.iwantjob.common.enums.PointReasonEnum;
import com.iwantjob.common.event.BadgeTriggerEvent;
import com.iwantjob.common.event.PointChangeEvent;
import com.iwantjob.common.exception.BusinessException;
import com.iwantjob.common.result.ErrorCode;
import com.iwantjob.common.result.PageResult;
import com.iwantjob.helpgroup.dto.HelpRequestCreateDTO;
import com.iwantjob.helpgroup.dto.HelpRequestListVO;
import com.iwantjob.helpgroup.dto.HelpRequestQueryDTO;
import com.iwantjob.helpgroup.dto.HelpRequestResolveDTO;
import com.iwantjob.helpgroup.dto.HelpRequestVO;
import com.iwantjob.helpgroup.entity.HelpGroupRequest;
import com.iwantjob.helpgroup.enums.HelpStatusEnum;
import com.iwantjob.helpgroup.mapper.HelpGroupRequestMapper;
import com.iwantjob.helpgroup.service.HelpGroupService;
import com.iwantjob.framework.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 帮帮团服务实现
 */
@Service
@RequiredArgsConstructor
public class HelpGroupServiceImpl implements HelpGroupService {

    private static final String ROLE_APPLICANT = "applicant";
    private static final String ROLE_SUPPORTER = "supporter";

    private final HelpGroupRequestMapper helpGroupRequestMapper;
    private final ApplicationEventPublisher publisher;

    @Override
    @Transactional
    public HelpRequestVO createRequest(HelpRequestCreateDTO dto) {
        Long userId = SecurityUtils.requireCurrentUserId();
        HelpGroupRequest entity = new HelpGroupRequest();
        entity.setApplicantId(userId);
        entity.setReasonType(dto.getReasonType());
        entity.setDescription(dto.getDescription());
        entity.setMatchTags(dto.getMatchTags());
        entity.setStatus(HelpStatusEnum.PENDING.getCode());
        helpGroupRequestMapper.insert(entity);
        HelpGroupRequest refreshed = helpGroupRequestMapper.selectById(entity.getId());
        return toVO(refreshed, ROLE_APPLICANT);
    }

    @Override
    public PageResult<HelpRequestListVO> pagePendingRequests(HelpRequestQueryDTO query) {
        Long userId = SecurityUtils.requireCurrentUserId();
        Page<HelpGroupRequest> page = new Page<>(query.getPage(), query.getSize());
        LambdaQueryWrapper<HelpGroupRequest> wrapper = new LambdaQueryWrapper<HelpGroupRequest>()
                .eq(HelpGroupRequest::getStatus, HelpStatusEnum.PENDING.getCode())
                .ne(HelpGroupRequest::getApplicantId, userId)
                .eq(query.getReasonType() != null, HelpGroupRequest::getReasonType, query.getReasonType())
                .like(query.getTag() != null && !query.getTag().isBlank(),
                        HelpGroupRequest::getMatchTags, query.getTag())
                .orderByDesc(HelpGroupRequest::getCreatedAt);
        IPage<HelpGroupRequest> result = helpGroupRequestMapper.selectPage(page, wrapper);
        List<HelpRequestListVO> records = result.getRecords().stream()
                .map(e -> toListVO(e, null))
                .collect(Collectors.toList());
        return PageResult.of(records, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    @Transactional
    public HelpRequestVO matchRequest(Long id) {
        Long currentUserId = SecurityUtils.requireCurrentUserId();
        HelpGroupRequest entity = loadOrThrow(id);
        // 状态必须为待匹配
        if (entity.getStatus() == null
                || entity.getStatus() != HelpStatusEnum.PENDING.getCode()) {
            throw new BusinessException(ErrorCode.HELP_ALREADY_MATCHED);
        }
        // 不能匹配自己的求助
        if (entity.getApplicantId().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "不能匹配自己的求助请求");
        }
        entity.setSupporterId(currentUserId);
        entity.setStatus(HelpStatusEnum.MATCHED.getCode());
        entity.setMatchedAt(LocalDateTime.now());
        helpGroupRequestMapper.updateById(entity);
        return toVO(entity, ROLE_SUPPORTER);
    }

    @Override
    public PageResult<HelpRequestListVO> pageMyRequests(HelpRequestQueryDTO query) {
        Long currentUserId = SecurityUtils.requireCurrentUserId();
        Page<HelpGroupRequest> page = new Page<>(query.getPage(), query.getSize());
        // 当前用户作为求助者或支援者的请求
        LambdaQueryWrapper<HelpGroupRequest> wrapper = new LambdaQueryWrapper<HelpGroupRequest>()
                .and(w -> w.eq(HelpGroupRequest::getApplicantId, currentUserId)
                        .or().eq(HelpGroupRequest::getSupporterId, currentUserId))
                .eq(query.getReasonType() != null, HelpGroupRequest::getReasonType, query.getReasonType())
                .like(query.getTag() != null && !query.getTag().isBlank(),
                        HelpGroupRequest::getMatchTags, query.getTag())
                .orderByDesc(HelpGroupRequest::getCreatedAt);
        IPage<HelpGroupRequest> result = helpGroupRequestMapper.selectPage(page, wrapper);
        List<HelpRequestListVO> records = result.getRecords().stream()
                .map(e -> toListVO(e, resolveRole(e, currentUserId)))
                .collect(Collectors.toList());
        return PageResult.of(records, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    @Transactional
    public HelpRequestVO resolveRequest(Long id, HelpRequestResolveDTO dto) {
        Long currentUserId = SecurityUtils.requireCurrentUserId();
        HelpGroupRequest entity = loadOrThrow(id);
        // 状态必须为已匹配
        if (entity.getStatus() == null
                || entity.getStatus() != HelpStatusEnum.MATCHED.getCode()) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED,
                    "当前求助状态不允许完成支援");
        }
        // 仅参与方（求助者或支援者）可完成支援
        boolean isApplicant = entity.getApplicantId() != null
                && entity.getApplicantId().equals(currentUserId);
        boolean isSupporter = entity.getSupporterId() != null
                && entity.getSupporterId().equals(currentUserId);
        if (!isApplicant && !isSupporter) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "仅参与方可完成支援");
        }
        // 写反馈并完成
        entity.setFeedback(dto.getFeedback());
        entity.setStatus(HelpStatusEnum.RESOLVED.getCode());
        entity.setResolvedAt(LocalDateTime.now());
        helpGroupRequestMapper.updateById(entity);

        // 触发徽章与积分事件，奖励支援者（帮助他人）
        if (entity.getSupporterId() != null) {
            publisher.publishEvent(new BadgeTriggerEvent(
                    entity.getSupporterId(), BadgeCondEnum.HELP_OTHERS, entity.getId()));
            publisher.publishEvent(new PointChangeEvent(
                    entity.getSupporterId(),
                    PointReasonEnum.HELP_RESOLVE.getDefaultPoints(),
                    PointReasonEnum.HELP_RESOLVE.getDesc(),
                    entity.getId()));
        }
        return toVO(entity, isApplicant ? ROLE_APPLICANT : ROLE_SUPPORTER);
    }

    /**
     * 加载求助请求，不存在则抛业务异常
     */
    private HelpGroupRequest loadOrThrow(Long id) {
        HelpGroupRequest entity = helpGroupRequestMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(ErrorCode.HELP_REQUEST_NOT_FOUND);
        }
        return entity;
    }

    /**
     * 根据当前用户与请求字段解析角色
     * 由于禁止匹配自己的求助，同一请求中 applicant 与 supporter 不会是同一人
     */
    private String resolveRole(HelpGroupRequest entity, Long currentUserId) {
        if (entity.getApplicantId() != null && entity.getApplicantId().equals(currentUserId)) {
            return ROLE_APPLICANT;
        }
        return ROLE_SUPPORTER;
    }

    private HelpRequestVO toVO(HelpGroupRequest entity, String role) {
        if (entity == null) {
            return null;
        }
        HelpRequestVO vo = new HelpRequestVO();
        BeanUtils.copyProperties(entity, vo);
        vo.setRole(role);
        return vo;
    }

    private HelpRequestListVO toListVO(HelpGroupRequest entity, String role) {
        if (entity == null) {
            return null;
        }
        HelpRequestListVO vo = new HelpRequestListVO();
        BeanUtils.copyProperties(entity, vo);
        vo.setRole(role);
        return vo;
    }
}
