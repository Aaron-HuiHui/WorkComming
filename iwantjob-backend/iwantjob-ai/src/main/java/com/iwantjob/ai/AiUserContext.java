package com.iwantjob.ai;

/**
 * AI 调用用户上下文。
 * <p>
 * iwantjob-ai 作为基础设施层，不依赖 iwantjob-framework，无法直接访问 SecurityUtils。
 * 业务模块在调用 {@link AiChatService#chat(String)} 等方法前，应通过 {@link #setCurrentUserId(Long)}
 * 注入当前用户 ID，以支持按用户限流；若未注入，AI 模块将跳过限流（不影响功能）。
 * <p>
 * 使用方式（业务模块示例）：
 * <pre>
 * AiUserContext.setCurrentUserId(currentUserId);
 * try {
 *     String reply = aiChatService.chat(prompt);
 * } finally {
 *     AiUserContext.clear();
 * }
 * </pre>
 */
public final class AiUserContext {

    private static final ThreadLocal<Long> CURRENT = new ThreadLocal<>();

    private AiUserContext() {
    }

    /**
     * 设置当前线程的调用用户 ID；传 null 等同于 clear
     */
    public static void setCurrentUserId(Long userId) {
        if (userId == null) {
            CURRENT.remove();
        } else {
            CURRENT.set(userId);
        }
    }

    /**
     * 获取当前线程的调用用户 ID，未设置时返回 null
     */
    public static Long getCurrentUserId() {
        return CURRENT.get();
    }

    /**
     * 清除当前线程上下文，防止线程池复用导致的串用户
     */
    public static void clear() {
        CURRENT.remove();
    }
}
