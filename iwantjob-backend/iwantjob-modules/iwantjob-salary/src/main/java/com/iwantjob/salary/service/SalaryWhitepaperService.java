package com.iwantjob.salary.service;

import com.iwantjob.salary.dto.WhitepaperSummaryVO;
import com.iwantjob.salary.dto.WhitepaperVO;

/**
 * 薪资白皮书服务接口
 */
public interface SalaryWhitepaperService {

    /**
     * 获取最新白皮书（简版公开，高级章节需贡献记录）
     *
     * @param userId 当前用户ID（用于判断是否解锁高级章节）
     * @return 白皮书（简版或完整版）
     */
    WhitepaperVO getLatest(Long userId);

    /**
     * 获取指定版本白皮书
     *
     * @param userId 当前用户ID
     * @param id     白皮书ID
     * @return 白皮书
     */
    WhitepaperVO getById(Long userId, Long id);

    /**
     * 手动触发白皮书生成
     * 聚合已审核数据，按城市/岗位/学历/行业分组计算 P25/P50/P75/P99 + 样本量，写 report_json
     *
     * @return 生成的白皮书ID
     */
    Long generateWhitepaper();
}
