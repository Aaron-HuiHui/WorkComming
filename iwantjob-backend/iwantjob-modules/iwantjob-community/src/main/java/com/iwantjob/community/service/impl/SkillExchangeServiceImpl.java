package com.iwantjob.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.iwantjob.common.exception.BusinessException;
import com.iwantjob.common.result.ErrorCode;
import com.iwantjob.community.dto.SkillExchangeCreateDTO;
import com.iwantjob.community.dto.SkillExchangeVO;
import com.iwantjob.community.entity.SkillExchange;
import com.iwantjob.community.enums.SkillExchangeStatusEnum;
import com.iwantjob.community.mapper.SkillExchangeMapper;
import com.iwantjob.community.service.SkillExchangeService;
import com.iwantjob.framework.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 技能交换服务实现
 */
@Service
@RequiredArgsConstructor
public class SkillExchangeServiceImpl implements SkillExchangeService {

    private final SkillExchangeMapper skillExchangeMapper;

    @Override
    @Transactional
    public SkillExchangeVO createSkillExchange(SkillExchangeCreateDTO dto) {
        Long userId = SecurityUtils.requireCurrentUserId();
        if (dto.getToUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "不能向自己发起技能交换");
        }
        SkillExchange exchange = new SkillExchange();
        exchange.setFromUserId(userId);
        exchange.setToUserId(dto.getToUserId());
        exchange.setOfferSkill(dto.getOfferSkill());
        exchange.setWantSkill(dto.getWantSkill());
        exchange.setStatus(SkillExchangeStatusEnum.PENDING.getCode());
        skillExchangeMapper.insert(exchange);
        return toVO(exchange);
    }

    @Override
    public List<SkillExchangeVO> mySkillExchanges() {
        Long userId = SecurityUtils.requireCurrentUserId();
        LambdaQueryWrapper<SkillExchange> wrapper = new LambdaQueryWrapper<SkillExchange>()
                .and(w -> w.eq(SkillExchange::getFromUserId, userId)
                        .or().eq(SkillExchange::getToUserId, userId))
                .orderByDesc(SkillExchange::getCreatedAt);
        return skillExchangeMapper.selectList(wrapper).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    private SkillExchangeVO toVO(SkillExchange exchange) {
        SkillExchangeVO vo = new SkillExchangeVO();
        BeanUtils.copyProperties(exchange, vo);
        return vo;
    }
}
