package com.iwantjob.salary.service;

import com.iwantjob.common.result.PageResult;
import com.iwantjob.salary.dto.SalaryContributeDTO;
import com.iwantjob.salary.dto.SalaryContributionVO;

/**
 * 薪资贡献服务接口
 */
public interface SalaryContributeService {

    /**
     * 提交脱敏薪资数据
     * - 校验薪资范围合理性
     * - Redis Set 去重（同用户同月同岗位）
     * - 入库 verified=0 待审核
     *
     * @param userId 贡献者用户ID
     * @param dto    脱敏薪资数据
     * @return 贡献记录ID
     */
    Long contribute(Long userId, SalaryContributeDTO dto);

    /**
     * 查询当前用户的贡献列表
     *
     * @param userId 用户ID
     * @param page   页码
     * @param size   每页条数
     * @return 分页贡献列表
     */
    PageResult<SalaryContributionVO> getMyContributions(Long userId, long page, long size);
}
