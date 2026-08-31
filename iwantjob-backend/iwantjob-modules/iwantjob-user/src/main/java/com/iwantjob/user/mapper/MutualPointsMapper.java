package com.iwantjob.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iwantjob.user.entity.MutualPoints;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 积分 Mapper
 * 说明：mutual_points 主键为 user_id，扣减使用自定义 SQL 实现乐观锁 + 余额校验
 */
@Mapper
public interface MutualPointsMapper extends BaseMapper<MutualPoints> {

    /**
     * 乐观锁扣减积分
     * 条件：user_id 匹配、version 匹配、余额 >= 扣减量
     * 影响0行：可能是版本冲突或余额不足，由调用方判断
     *
     * @param userId    用户ID
     * @param points    扣减积分数（正数）
     * @param version   当前版本号
     * @return 影响行数（1成功，0失败）
     */
    @Update("UPDATE mutual_points SET balance = balance - #{points}, " +
            "version = version + 1, updated_at = NOW() " +
            "WHERE user_id = #{userId} AND version = #{version} AND balance >= #{points}")
    int deductPoints(@Param("userId") Long userId,
                     @Param("points") int points,
                     @Param("version") int version);

    /**
     * 乐观锁增加积分
     *
     * @param userId    用户ID
     * @param points    增加积分数（正数）
     * @param version   当前版本号
     * @return 影响行数
     */
    @Update("UPDATE mutual_points SET balance = balance + #{points}, " +
            "total_earned = total_earned + #{points}, version = version + 1, updated_at = NOW() " +
            "WHERE user_id = #{userId} AND version = #{version}")
    int addPoints(@Param("userId") Long userId,
                  @Param("points") int points,
                  @Param("version") int version);
}
