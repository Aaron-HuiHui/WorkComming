package com.iwantjob.portfolio.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iwantjob.common.exception.BusinessException;
import com.iwantjob.common.result.ErrorCode;
import com.iwantjob.common.result.PageResult;
import com.iwantjob.portfolio.dto.PortfolioSaveDTO;
import com.iwantjob.portfolio.dto.PortfolioVO;
import com.iwantjob.portfolio.entity.Portfolio;
import com.iwantjob.portfolio.entity.PortfolioLike;
import com.iwantjob.portfolio.mapper.PortfolioLikeMapper;
import com.iwantjob.portfolio.mapper.PortfolioMapper;
import com.iwantjob.portfolio.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 作品集服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PortfolioServiceImpl implements PortfolioService {

    private final PortfolioMapper portfolioMapper;
    private final PortfolioLikeMapper portfolioLikeMapper;

    @Override
    public PageResult<PortfolioVO> pagePortfolios(Long currentUserId, long page, long size, String tag) {
        Page<PortfolioVO> p = new Page<>(page, size);
        IPage<PortfolioVO> result = portfolioMapper.selectPortfolioPage(p, currentUserId, tag, null);
        return PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public PageResult<PortfolioVO> pageMyPortfolios(Long userId, long page, long size) {
        Page<PortfolioVO> p = new Page<>(page, size);
        IPage<PortfolioVO> result = portfolioMapper.selectPortfolioPage(p, userId, null, userId);
        return PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public PortfolioVO getPortfolioDetail(Long currentUserId, Long id) {
        Portfolio po = portfolioMapper.selectById(id);
        if (po == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        PortfolioLike like = portfolioLikeMapper.selectOne(
                new LambdaQueryWrapper<PortfolioLike>()
                        .eq(PortfolioLike::getPortfolioId, id)
                        .eq(PortfolioLike::getUserId, currentUserId));
        PortfolioVO vo = new PortfolioVO();
        BeanUtils.copyProperties(po, vo);
        vo.setLiked(like != null);
        portfolioMapper.incrementViewCount(id);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPortfolio(Long userId, PortfolioSaveDTO dto) {
        Portfolio po = new Portfolio();
        BeanUtils.copyProperties(dto, po);
        po.setUserId(userId);
        po.setViewCount(0);
        po.setLikeCount(0);
        portfolioMapper.insert(po);
        log.info("发布作品成功: id={}, userId={}, title={}", po.getId(), userId, po.getTitle());
        return po.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePortfolio(Long userId, Long id, PortfolioSaveDTO dto) {
        Portfolio po = requireOwner(userId, id);
        BeanUtils.copyProperties(dto, po);
        portfolioMapper.updateById(po);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePortfolio(Long userId, Long id) {
        requireOwner(userId, id);
        portfolioMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Object[] toggleLike(Long userId, Long portfolioId) {
        Portfolio po = portfolioMapper.selectById(portfolioId);
        if (po == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        int current = po.getLikeCount() == null ? 0 : po.getLikeCount();
        PortfolioLike existing = portfolioLikeMapper.selectOne(
                new LambdaQueryWrapper<PortfolioLike>()
                        .eq(PortfolioLike::getPortfolioId, portfolioId)
                        .eq(PortfolioLike::getUserId, userId));
        Portfolio upd = new Portfolio();
        upd.setId(portfolioId);
        if (existing != null) {
            portfolioLikeMapper.deleteById(existing.getId());
            upd.setLikeCount(Math.max(0, current - 1));
            portfolioMapper.updateById(upd);
            return new Object[]{false, upd.getLikeCount()};
        }
        PortfolioLike like = new PortfolioLike();
        like.setPortfolioId(portfolioId);
        like.setUserId(userId);
        portfolioLikeMapper.insert(like);
        upd.setLikeCount(current + 1);
        portfolioMapper.updateById(upd);
        return new Object[]{true, upd.getLikeCount()};
    }

    /**
     * 校验作品存在且属于当前用户
     */
    private Portfolio requireOwner(Long userId, Long id) {
        Portfolio po = portfolioMapper.selectById(id);
        if (po == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        if (!po.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return po;
    }
}