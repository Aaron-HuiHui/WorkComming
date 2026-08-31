package com.iwantjob.helpgroup.service;

import com.iwantjob.common.result.PageResult;
import com.iwantjob.helpgroup.dto.HelpRequestCreateDTO;
import com.iwantjob.helpgroup.dto.HelpRequestListVO;
import com.iwantjob.helpgroup.dto.HelpRequestQueryDTO;
import com.iwantjob.helpgroup.dto.HelpRequestResolveDTO;
import com.iwantjob.helpgroup.dto.HelpRequestVO;

/**
 * 帮帮团服务
 */
public interface HelpGroupService {

    /**
     * 发起求助（applicant_id=当前用户，初始状态待匹配）
     */
    HelpRequestVO createRequest(HelpRequestCreateDTO dto);

    /**
     * 待匹配求助列表（分页，排除自己发起的）
     */
    PageResult<HelpRequestListVO> pagePendingRequests(HelpRequestQueryDTO query);

    /**
     * 匹配支援者（当前用户成为 supporter，状态 0→1）
     */
    HelpRequestVO matchRequest(Long id);

    /**
     * 我的求助 / 我支援的（按当前用户在每条请求中的角色查询并合并返回）
     */
    PageResult<HelpRequestListVO> pageMyRequests(HelpRequestQueryDTO query);

    /**
     * 完成支援（写 feedback，状态 1→2，触发徽章与积分事件给 supporter）
     */
    HelpRequestVO resolveRequest(Long id, HelpRequestResolveDTO dto);
}
