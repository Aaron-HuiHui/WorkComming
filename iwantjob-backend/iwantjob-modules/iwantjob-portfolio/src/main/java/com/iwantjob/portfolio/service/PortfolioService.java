package com.iwantjob.portfolio.service;

import com.iwantjob.common.result.PageResult;
import com.iwantjob.portfolio.dto.PortfolioSaveDTO;
import com.iwantjob.portfolio.dto.PortfolioVO;

/**
 * 作品集服务接口
 */
public interface PortfolioService {

    /** 作品广场分页（可按技术标签过滤） */
    PageResult<PortfolioVO> pagePortfolios(Long currentUserId, long page, long size, String tag);

    /** 我的作品分页 */
    PageResult<PortfolioVO> pageMyPortfolios(Long userId, long page, long size);

    /** 作品详情（浏览量+1） */
    PortfolioVO getPortfolioDetail(Long currentUserId, Long id);

    /** 发布作品 */
    Long createPortfolio(Long userId, PortfolioSaveDTO dto);

    /** 更新作品（仅作者本人） */
    void updatePortfolio(Long userId, Long id, PortfolioSaveDTO dto);

    /** 删除作品（软删除，仅作者本人） */
    void deletePortfolio(Long userId, Long id);

    /** 点赞/取消点赞切换，返回 [是否已赞, 最新点赞数] */
    Object[] toggleLike(Long userId, Long portfolioId);
}