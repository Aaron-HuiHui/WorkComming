package com.iwantjob.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iwantjob.community.entity.CrowdfundingProject;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;

/**
 * 众筹项目 Mapper
 */
@Mapper
public interface CrowdfundingProjectMapper extends BaseMapper<CrowdfundingProject> {

    /**
     * 当前已筹金额自增（支持众筹接口调用，原子操作避免并发超卖）
     */
    @Update("UPDATE crowdfunding_project SET current_amount = current_amount + #{amount} " +
            "WHERE id = #{id} AND is_deleted = 0")
    int incrementCurrentAmount(@Param("id") Long id, @Param("amount") BigDecimal amount);
}
