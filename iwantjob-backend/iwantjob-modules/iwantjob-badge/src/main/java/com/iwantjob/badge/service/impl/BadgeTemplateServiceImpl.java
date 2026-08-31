package com.iwantjob.badge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.iwantjob.badge.dto.BadgeTemplateCreateDTO;
import com.iwantjob.badge.dto.BadgeTemplateVO;
import com.iwantjob.badge.entity.BadgeTemplate;
import com.iwantjob.badge.mapper.BadgeTemplateMapper;
import com.iwantjob.badge.service.BadgeTemplateService;
import com.iwantjob.common.enums.BadgeCondEnum;
import com.iwantjob.common.enums.RarityEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 徽章模板服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BadgeTemplateServiceImpl implements BadgeTemplateService {

    private final BadgeTemplateMapper badgeTemplateMapper;

    @Override
    public List<BadgeTemplateVO> listTemplates() {
        List<BadgeTemplate> templates = badgeTemplateMapper.selectList(null);
        return templates.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public BadgeTemplate getById(Long templateId) {
        BadgeTemplate t = badgeTemplateMapper.selectById(templateId);
        if (t == null) {
            log.warn("徽章模板不存在: id={}", templateId);
        }
        return t;
    }

    @Override
    public Long createTemplate(BadgeTemplateCreateDTO dto) {
        BadgeTemplate t = new BadgeTemplate();
        t.setName(dto.getName());
        t.setDescription(dto.getDescription());
        t.setIconUrl(dto.getIconUrl());
        t.setConditionType(dto.getConditionType());
        t.setThreshold(dto.getThreshold());
        t.setRarity(dto.getRarity());
        badgeTemplateMapper.insert(t);
        log.info("新建徽章模板: id={}, name={}, conditionType={}, threshold={}",
                t.getId(), t.getName(), t.getConditionType(), t.getThreshold());
        return t.getId();
    }

    @Override
    public List<BadgeTemplate> listByConditionType(Integer conditionType) {
        return badgeTemplateMapper.selectList(
                new LambdaQueryWrapper<BadgeTemplate>()
                        .eq(BadgeTemplate::getConditionType, conditionType));
    }

    private BadgeTemplateVO toVO(BadgeTemplate t) {
        BadgeTemplateVO vo = new BadgeTemplateVO();
        vo.setId(t.getId());
        vo.setName(t.getName());
        vo.setDescription(t.getDescription());
        vo.setIconUrl(t.getIconUrl());
        vo.setConditionType(t.getConditionType());
        vo.setThreshold(t.getThreshold());
        vo.setRarity(t.getRarity());
        if (t.getConditionType() != null) {
            try {
                vo.setConditionDesc(BadgeCondEnum.of(t.getConditionType()).getDesc());
            } catch (IllegalArgumentException ignored) {
                vo.setConditionDesc("未知");
            }
        }
        if (t.getRarity() != null) {
            try {
                vo.setRarityDesc(RarityEnum.of(t.getRarity()).getDesc());
            } catch (IllegalArgumentException ignored) {
                vo.setRarityDesc("未知");
            }
        }
        return vo;
    }
}
