package com.iwantjob.salary.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iwantjob.salary.entity.SalaryReportData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 薪资贡献数据 Mapper
 */
@Mapper
public interface SalaryReportDataMapper extends BaseMapper<SalaryReportData> {

    /**
     * 查询某城市/岗位下已审核通过的有效薪资中位数（用于 3σ 异常检测）
     * 返回每条记录的薪资均值：(salary_min + salary_max) / 2
     *
     * @param city     城市
     * @param position 岗位
     * @return 薪资均值列表
     */
    @Select("SELECT (salary_min + salary_max) / 2 AS avg_salary " +
            "FROM salary_report_data " +
            "WHERE city = #{city} AND position = #{position} " +
            "AND verified = 1 AND is_deleted = 0 " +
            "AND salary_min IS NOT NULL AND salary_max IS NOT NULL")
    List<Integer> selectApprovedAvgSalaries(@Param("city") String city,
                                            @Param("position") String position);

    /**
     * 统计用户已审核通过的有效贡献次数（用于判断是否解锁精准匹配）
     *
     * @param userId 用户ID
     * @return 有效贡献次数
     */
    @Select("SELECT COUNT(1) FROM salary_report_data " +
            "WHERE user_id = #{userId} AND verified = 1 AND is_deleted = 0")
    int countApprovedByUser(@Param("userId") Long userId);
}
