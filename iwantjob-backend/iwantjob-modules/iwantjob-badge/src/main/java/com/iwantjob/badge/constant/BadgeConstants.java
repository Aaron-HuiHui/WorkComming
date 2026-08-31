package com.iwantjob.badge.constant;

/**
 * 徽章模块常量
 */
public final class BadgeConstants {

    private BadgeConstants() {}

    /**
     * Redis 计数器 key 前缀：badge:count:{userId}:{conditionType}
     */
    public static final String COUNT_KEY_PREFIX = "badge:count:";

    /**
     * 徽章触发事件去重 key 前缀：badge:event:dedup:{refId}
     */
    public static final String EVENT_DEDUP_KEY_PREFIX = "badge:event:dedup:";

    /**
     * 锁定动作
     */
    public static final String ACTION_LOCK = "LOCK";

    /**
     * 哈希指纹展示长度（lock_hash 前 8 位）
     */
    public static final int FINGERPRINT_LENGTH = 8;

    /**
     * SHA-256 算法名
     */
    public static final String SHA_ALGORITHM = "SHA-256";
}
