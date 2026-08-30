package com.iwantjob.ai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * AI 对话消息模型，与千问角色映射：system / user / assistant
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessage implements Serializable {

    /** 角色："system" / "user" / "assistant" */
    private String role;

    /** 文本内容 */
    private String content;

    public static ChatMessage system(String content) {
        return new ChatMessage("system", content);
    }

    public static ChatMessage user(String content) {
        return new ChatMessage("user", content);
    }

    public static ChatMessage assistant(String content) {
        return new ChatMessage("assistant", content);
    }
}
