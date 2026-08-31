package com.iwantjob.salary.service;

import com.iwantjob.common.result.PageResult;
import com.iwantjob.salary.dto.PendingSalaryVO;
import com.iwantjob.salary.dto.SalaryReviewDTO;
import com.iwantjob.salary.dto.SalaryReviewLogVO;

import java.util.List;

/**
 * 薪资审核服务接口
 */
public interface SalaryReviewService {

    /**
     * 待审核薪资数据列表（含 3σ 异常标记）
     *
     * @param page 页码
     * @param size 每页条数
     * @return 分页待审核列表
     */
    PageResult<PendingSalaryVO> getPendingList(long page, long size);

    /**
     * 审核薪资数据
     * - APPROVE: 更新verified=1 + 写review_log + 发PointChangeEvent(30分) + 写contribution_reward
     * - REJECT:  更新verified=2 + 写review_log
     *
     * @param reviewerId   审核人ID
     * @param reportDataId 薪资数据ID
     * @param dto          审核请求
     */
    void review(Long reviewerId, Long reportDataId, SalaryReviewDTO dto);

    /**
     * 查询某条薪资数据的审核日志
     *
     * @param reportDataId 薪资数据ID
     * @return 审核日志列表
     */
    List<SalaryReviewLogVO> getReviewLogs(Long reportDataId);
}
