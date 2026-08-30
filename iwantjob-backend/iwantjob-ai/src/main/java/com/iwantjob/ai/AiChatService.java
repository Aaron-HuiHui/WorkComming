package com.iwantjob.ai;

import java.util.List;

/**
 * AI 基础设施服务，仅暴露千问调用原子能力，不做业务 prompt 编排。
 * <p>
 * 业务模块（resume / interview / simulator 等）按需调用并自行组织 prompt。
 */
public interface AiChatService {

    /**
     * 单轮对话
     */
    String chat(String prompt);

    /**
     * 多轮对话（带历史消息）
     *
     * @param prompt  本轮用户输入
     * @param history 历史消息，可为 null 或空
     */
    String chat(String prompt, List<ChatMessage> history);

    /**
     * 流式对话，逐字回调
     *
     * @param prompt    本轮用户输入
     * @param history   历史消息，可为 null 或空
     * @param callback  流式回调
     */
    void chatStream(String prompt, List<ChatMessage> history, StreamCallback callback);
}
