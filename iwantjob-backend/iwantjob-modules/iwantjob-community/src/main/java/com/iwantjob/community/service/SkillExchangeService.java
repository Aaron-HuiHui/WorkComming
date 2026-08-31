package com.iwantjob.community.service;

import com.iwantjob.community.dto.SkillExchangeCreateDTO;
import com.iwantjob.community.dto.SkillExchangeVO;

import java.util.List;

/**
 * 技能交换服务
 */
public interface SkillExchangeService {

    /**
     * 发起技能交换
     */
    SkillExchangeVO createSkillExchange(SkillExchangeCreateDTO dto);

    /**
     * 我的技能交换（作为发起方或目标方）
     */
    List<SkillExchangeVO> mySkillExchanges();
}
