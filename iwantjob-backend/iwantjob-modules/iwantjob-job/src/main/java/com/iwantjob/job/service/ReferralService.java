package com.iwantjob.job.service;

import com.iwantjob.job.dto.ReferralCreateDTO;
import com.iwantjob.job.dto.ReferralVO;

/**
 * 内推服务接口
 */
public interface ReferralService {

    /**
     * 创建内推码（生成唯一 referral_code）
     */
    ReferralVO createReferral(Long userId, ReferralCreateDTO dto);
}
