package com.iwantjob.portfolio.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iwantjob.portfolio.entity.PortfolioLike;
import org.apache.ibatis.annotations.Mapper;

/**
 * 作品点赞 Mapper
 */
@Mapper
public interface PortfolioLikeMapper extends BaseMapper<PortfolioLike> {
}