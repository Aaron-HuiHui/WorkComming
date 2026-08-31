package com.iwantjob.badge.service;

import com.iwantjob.badge.dto.BadgeTemplateCreateDTO;
import com.iwantjob.badge.dto.BadgeTemplateVO;
import com.iwantjob.badge.entity.BadgeTemplate;

import java.util.List;

/**
 * 徽章模板服务接口
 */
public interface BadgeTemplateService {

    /**
     * 查询全部徽章模板（已登录可见）
     */
    List<BadgeTemplateVO> listTemplates();

    /**
     * 按 ID 查询模板实体
     */
    BadgeTemplate getById(Long templateId);

    /**
     * 新建徽章模板（管理员）
     */
    Long createTemplate(BadgeTemplateCreateDTO dto);

    /**
     * 按条件类型查询匹配的模板列表（铸造判定用）
     */
    List<BadgeTemplate> listByConditionType(Integer conditionType);
}
