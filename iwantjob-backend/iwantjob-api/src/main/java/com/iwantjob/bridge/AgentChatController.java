package com.iwantjob.bridge;

import com.iwantjob.ai.AiChatService;
import com.iwantjob.ai.AiUserContext;
import com.iwantjob.ai.ChatMessage;
import com.iwantjob.common.result.Result;
import com.iwantjob.framework.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * AI 智能体自由问答（聚合层实现：直接复用 iwantjob-ai 的 AiChatService）。
 * <p>
 * 与模拟舱的场景剧情流不同,本接口对<b>任意问题</b>开放——职业规划、面试疑问、
 * 简历措辞、行业咨询、职场困惑均可直接提问,由通用求职智能体回答。
 * 历史对话上下文由前端回传,保持多轮连贯。
 * <p>
 * 权限:所有登录角色(学生/校友/HR/导师/管理员)。
 */
@Slf4j
@RestController
@RequestMapping("/agent")
@RequiredArgsConstructor
@Tag(name = "AI 智能体", description = "求职领域自由问答:任意问题、多轮对话、不限场景")
public class AgentChatController {

    private final AiChatService aiChatService;

    /** 智能体人设:求职教练,但允许超纲问题 */
    private static final String SYSTEM_PROMPT =
            "你是「我要工作」平台的 AI 求职智能体,一名经验丰富的职业教练。"
            + "用户可能问任何问题——求职策略、面试准备、简历优化、职业规划、行业分析、"
            + "薪资谈判、职场人际关系,或与求职无关的一般问题。"
            + "回答要求:中文,直接给出实用、具体、可操作的建议;"
            + "适当使用分点与加粗强调重点;语气亲切专业,不说套话。";

    @PostMapping("/ask")
    @Operation(summary = "自由问答:任意问题 + 多轮历史,智能体直接回答")
    public Result<String> ask(@Valid @RequestBody AskDTO dto) {
        Long userId = SecurityUtils.requireCurrentUserId();
        try {
            AiUserContext.setCurrentUserId(userId);
            List<ChatMessage> history = toHistory(dto.history);
            String answer = aiChatService.chat(dto.question, history);
            return Result.success(answer);
        } finally {
            AiUserContext.setCurrentUserId(null);
        }
    }

    private List<ChatMessage> toHistory(List<AskMessageDTO> raw) {
        List<ChatMessage> history = new ArrayList<>();
        if (raw != null && !raw.isEmpty()) {
            // 携带系统人设 + 最近 10 轮,避免 token 膨胀
            history.add(ChatMessage.system(SYSTEM_PROMPT));
            int from = Math.max(0, raw.size() - 20);
            for (int i = from; i < raw.size(); i++) {
                AskMessageDTO m = raw.get(i);
                if (m == null || m.getContent() == null || m.getContent().isBlank()) {
                    continue;
                }
                if ("assistant".equals(m.getRole())) {
                    history.add(ChatMessage.assistant(m.getContent()));
                } else {
                    history.add(ChatMessage.user(m.getContent()));
                }
            }
        } else {
            history.add(ChatMessage.system(SYSTEM_PROMPT));
        }
        return history;
    }

    @Data
    public static class AskDTO {
        /** 用户本轮问题(任意内容) */
        private String question;
        /** 历史对话(user/assistant 交替),可为空 */
        private List<AskMessageDTO> history;
    }

    @Data
    public static class AskMessageDTO {
        /** "user" / "assistant" */
        private String role;
        private String content;
    }
}
