package com.iwantjob.community.service;

import com.iwantjob.community.dto.CrowdfundingCreateDTO;
import com.iwantjob.community.dto.CrowdfundingSupportDTO;
import com.iwantjob.community.dto.CrowdfundingVO;

/**
 * 众筹服务
 */
public interface CrowdfundingService {

    /**
     * 发起众筹
     */
    CrowdfundingVO createCrowdfunding(CrowdfundingCreateDTO dto);

    /**
     * 支持众筹
     */
    CrowdfundingVO supportCrowdfunding(Long id, CrowdfundingSupportDTO dto);
}
