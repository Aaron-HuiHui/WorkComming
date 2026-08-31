package com.iwantjob.badge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.iwantjob.badge.constant.BadgeConstants;
import com.iwantjob.badge.dto.BadgeVerifyVO;
import com.iwantjob.badge.dto.UserBadgeVO;
import com.iwantjob.badge.entity.BadgeLockLog;
import com.iwantjob.badge.entity.BadgeTemplate;
import com.iwantjob.badge.entity.UserBadge;
import com.iwantjob.badge.mapper.BadgeLockLogMapper;
import com.iwantjob.badge.mapper.BadgeTemplateMapper;
import com.iwantjob.badge.mapper.UserBadgeMapper;
import com.iwantjob.badge.service.BadgeService;
import com.iwantjob.badge.service.BadgeTemplateService;
import com.iwantjob.common.enums.RarityEnum;
import com.iwantjob.common.exception.BusinessException;
import com.iwantjob.common.result.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 徽章业务服务实现
 * <p>
 * 防篡改可信徽章核心机制：
 * 1. 事件驱动铸造：监听 {@link com.iwantjob.common.event.BadgeTriggerEvent}，
 *    Redis INCR 计数器 key=badge:count:{userId}:{conditionType}，
 *    达 threshold 且未持有 → 铸造。
 * 2. 铸造流程：lock_hash=SHA256(userId+badgeId+earnedAt+salt)，
 *    INSERT user_badge(is_locked=1, lock_hash)，INSERT badge_lock_log(action=LOCK, operated_by=null)。
 * 3. 防篡改：user_badge 表 DB 触发器禁止 UPDATE/DELETE，Java 层不提供修改/删除接口。
 * 4. 企业查验：校验传入 hash 与 DB lock_hash 一致，并重新计算哈希校验 DB 内部一致性。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BadgeServiceImpl implements BadgeService {

    private final UserBadgeMapper userBadgeMapper;
    private final BadgeTemplateMapper badgeTemplateMapper;
    private final BadgeLockLogMapper badgeLockLogMapper;
    private final BadgeTemplateService badgeTemplateService;
    private final StringRedisTemplate redisTemplate;

    /**
     * 哈希盐值，从配置读取，默认 iwantjob-badge-salt-2026
     */
    @Value("${badge.salt:iwantjob-badge-salt-2026}")
    private String salt;

    /**
     * earnedAt 与哈希运算保持一致的格式
     */
    private static final DateTimeFormatter EARNED_AT_FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public void handleTriggerEvent(Long userId, Integer conditionType, Long refId) {
        if (userId == null || conditionType == null) {
            log.warn("徽章触发事件参数缺失: userId={}, conditionType={}", userId, conditionType);
            return;
        }
        // 1. refId 去重，防止同一业务事件被重复计数（refId 为 null 时不去重）
        if (refId != null) {
            String dedupKey = BadgeConstants.EVENT_DEDUP_KEY_PREFIX + refId;
            Boolean first = redisTemplate.opsForValue().setIfAbsent(dedupKey, "1", 30, TimeUnit.DAYS);
            if (Boolean.FALSE.equals(first)) {
                log.debug("徽章触发事件已处理过，跳过: userId={}, refId={}", userId, refId);
                return;
            }
        }
        // 2. Redis 计数器 INCR
        String countKey = BadgeConstants.COUNT_KEY_PREFIX + userId + ":" + conditionType;
        Long count = redisTemplate.opsForValue().increment(countKey);
        if (count == null) {
            log.warn("Redis 计数器返回 null: key={}", countKey);
            return;
        }
        log.info("徽章计数: userId={}, conditionType={}, count={}", userId, conditionType, count);

        // 3. 查询匹配该条件类型的徽章模板
        List<BadgeTemplate> templates = badgeTemplateService.listByConditionType(conditionType);
        if (templates.isEmpty()) {
            log.debug("无匹配徽章模板: conditionType={}", conditionType);
            return;
        }

        // 4. 逐个判定是否达阈值且未持有 → 铸造
        for (BadgeTemplate t : templates) {
            if (t.getThreshold() != null && count >= t.getThreshold()) {
                if (!ownsBadge(userId, t.getId())) {
                    try {
                        Long ubId = mintBadge(userId, t.getId());
                        log.info("徽章铸造成功: userId={}, badgeId={}, userBadgeId={}, count={}, threshold={}",
                                userId, t.getId(), ubId, count, t.getThreshold());
                    } catch (BusinessException e) {
                        // 已拥有则跳过
                        if (ErrorCode.BADGE_ALREADY_OWNED.getCode() != e.getCode()) {
                            log.error("徽章铸造异常: userId={}, badgeId={}, err={}", userId, t.getId(), e.getMessage());
                        }
                    } catch (Exception e) {
                        log.error("徽章铸造异常: userId={}, badgeId={}, err={}", userId, t.getId(), e.getMessage(), e);
                    }
                }
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long mintBadge(Long userId, Long badgeId) {
        // 1. 校验模板存在
        BadgeTemplate template = badgeTemplateMapper.selectById(badgeId);
        if (template == null) {
            throw new BusinessException(ErrorCode.BADGE_NOT_FOUND);
        }
        // 2. 幂等校验：同一用户同一徽章仅能铸造一次
        if (ownsBadge(userId, badgeId)) {
            throw new BusinessException(ErrorCode.BADGE_ALREADY_OWNED);
        }
        // 3. 计算哈希：lock_hash = SHA256(userId + badgeId + earnedAt + salt)
        LocalDateTime earnedAt = LocalDateTime.now();
        String lockHash = computeLockHash(userId, badgeId, earnedAt);
        // 4. INSERT user_badge（is_locked=1，铸造即锁定，不可回退）
        UserBadge ub = new UserBadge();
        ub.setUserId(userId);
        ub.setBadgeId(badgeId);
        ub.setEarnedAt(earnedAt);
        ub.setIsLocked(1);
        ub.setLockHash(lockHash);
        userBadgeMapper.insert(ub);
        // 5. INSERT badge_lock_log（action=LOCK, operated_by=null 表示系统自动）
        BadgeLockLog lockLog = new BadgeLockLog();
        lockLog.setUserBadgeId(ub.getId());
        lockLog.setOperatedBy(null);
        lockLog.setAction(BadgeConstants.ACTION_LOCK);
        badgeLockLogMapper.insert(lockLog);
        log.info("徽章铸造完成: userId={}, badgeId={}, userBadgeId={}, lockHash={}",
                userId, badgeId, ub.getId(), lockHash);
        return ub.getId();
    }

    @Override
    public List<UserBadgeVO> listUserBadges(Long userId) {
        return buildUserBadgeVOs(userId);
    }

    @Override
    public List<UserBadgeVO> listMyBadges(Long userId) {
        return buildUserBadgeVOs(userId);
    }

    @Override
    public BadgeVerifyVO verify(Long userId, Long badgeId, String hash) {
        BadgeVerifyVO vo = new BadgeVerifyVO();
        vo.setUserId(userId);
        vo.setBadgeId(badgeId);
        if (userId == null || badgeId == null || hash == null || hash.isBlank()) {
            vo.setValid(false);
            vo.setMessage("查验参数缺失");
            return vo;
        }
        // 1. 查 user_badge
        UserBadge ub = findUserBadge(userId, badgeId);
        if (ub == null) {
            vo.setValid(false);
            vo.setMessage("用户未持有该徽章");
            return vo;
        }
        // 2. 校验传入 hash 与 DB lock_hash 字符串一致
        boolean inputMatch = hash.equals(ub.getLockHash());
        // 3. 重新计算哈希校验 DB 内部一致性（防篡改）
        boolean internalConsistent = false;
        try {
            String recomputed = computeLockHash(ub.getUserId(), ub.getBadgeId(), ub.getEarnedAt());
            internalConsistent = recomputed.equals(ub.getLockHash());
        } catch (Exception e) {
            log.error("重新计算哈希失败: userBadgeId={}", ub.getId(), e);
        }
        // 4. 填充徽章信息
        BadgeTemplate t = badgeTemplateMapper.selectById(badgeId);
        if (t != null) {
            vo.setName(t.getName());
            vo.setDescription(t.getDescription());
            vo.setIconUrl(t.getIconUrl());
            vo.setRarity(t.getRarity());
        }
        vo.setEarnedAt(ub.getEarnedAt());
        vo.setIsLocked(ub.getIsLocked());

        if (inputMatch && internalConsistent) {
            vo.setValid(true);
            vo.setMessage("徽章有效，哈希指纹校验通过");
        } else if (!inputMatch) {
            vo.setValid(false);
            vo.setMessage("哈希指纹不匹配");
        } else {
            vo.setValid(false);
            vo.setMessage("徽章记录已被篡改，DB 内部一致性校验失败");
        }
        return vo;
    }

    // ============= 内部辅助方法 =============

    /**
     * 查询用户徽章并组装 VO（含 lock_hash 前 8 位指纹）
     */
    private List<UserBadgeVO> buildUserBadgeVOs(Long userId) {
        List<UserBadge> userBadges = userBadgeMapper.selectList(
                new LambdaQueryWrapper<UserBadge>()
                        .eq(UserBadge::getUserId, userId)
                        .orderByDesc(UserBadge::getEarnedAt));
        if (userBadges.isEmpty()) {
            return Collections.emptyList();
        }
        // 批量查模板
        Set<Long> templateIds = userBadges.stream().map(UserBadge::getBadgeId).collect(Collectors.toSet());
        Map<Long, BadgeTemplate> templateMap = badgeTemplateMapper.selectBatchIds(templateIds).stream()
                .collect(Collectors.toMap(BadgeTemplate::getId, t -> t, (a, b) -> a));
        return userBadges.stream().map(ub -> toVO(ub, templateMap.get(ub.getBadgeId()))).collect(Collectors.toList());
    }

    private UserBadgeVO toVO(UserBadge ub, BadgeTemplate t) {
        UserBadgeVO vo = new UserBadgeVO();
        vo.setId(ub.getId());
        vo.setUserId(ub.getUserId());
        vo.setBadgeId(ub.getBadgeId());
        vo.setEarnedAt(ub.getEarnedAt());
        vo.setIsLocked(ub.getIsLocked());
        // 公开场景仅暴露 lock_hash 前 8 位指纹
        if (ub.getLockHash() != null && ub.getLockHash().length() >= BadgeConstants.FINGERPRINT_LENGTH) {
            vo.setFingerprint(ub.getLockHash().substring(0, BadgeConstants.FINGERPRINT_LENGTH));
        }
        if (t != null) {
            vo.setName(t.getName());
            vo.setDescription(t.getDescription());
            vo.setIconUrl(t.getIconUrl());
            vo.setRarity(t.getRarity());
            if (t.getRarity() != null) {
                try {
                    vo.setRarityDesc(RarityEnum.of(t.getRarity()).getDesc());
                } catch (IllegalArgumentException ignored) {
                    vo.setRarityDesc("未知");
                }
            }
        }
        return vo;
    }

    /**
     * 查询用户是否已持有某徽章（uk_user_badge 唯一约束）
     */
    private boolean ownsBadge(Long userId, Long badgeId) {
        Long cnt = userBadgeMapper.selectCount(
                new LambdaQueryWrapper<UserBadge>()
                        .eq(UserBadge::getUserId, userId)
                        .eq(UserBadge::getBadgeId, badgeId));
        return cnt != null && cnt > 0;
    }

    /**
     * 按 userId + badgeId 查询用户徽章记录
     */
    private UserBadge findUserBadge(Long userId, Long badgeId) {
        return userBadgeMapper.selectOne(
                new LambdaQueryWrapper<UserBadge>()
                        .eq(UserBadge::getUserId, userId)
                        .eq(UserBadge::getBadgeId, badgeId));
    }

    /**
     * 计算锁定哈希：SHA256(userId + badgeId + earnedAt + salt)
     * <p>
     * earnedAt 使用 ISO_LOCAL_DATE_TIME 格式，保证存库值与哈希运算值一致。
     */
    private String computeLockHash(Long userId, Long badgeId, LocalDateTime earnedAt) {
        String input = userId + ":" + badgeId + ":" + earnedAt.format(EARNED_AT_FMT) + ":" + salt;
        try {
            MessageDigest md = MessageDigest.getInstance(BadgeConstants.SHA_ALGORITHM);
            byte[] hashBytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 为 JDK 内置算法，理论上不会缺失
            throw new IllegalStateException("SHA-256 算法不可用", e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
