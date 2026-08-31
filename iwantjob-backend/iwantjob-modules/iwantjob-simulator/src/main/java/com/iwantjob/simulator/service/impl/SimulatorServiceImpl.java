package com.iwantjob.simulator.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iwantjob.common.enums.BadgeCondEnum;
import com.iwantjob.common.event.BadgeTriggerEvent;
import com.iwantjob.common.event.PointChangeEvent;
import com.iwantjob.common.exception.BusinessException;
import com.iwantjob.common.result.ErrorCode;
import com.iwantjob.common.result.PageResult;
import com.iwantjob.simulator.ai.AiFeedback;
import com.iwantjob.simulator.ai.AiReport;
import com.iwantjob.simulator.ai.SimulatorAiGateway;
import com.iwantjob.simulator.dto.ChooseDTO;
import com.iwantjob.simulator.dto.ChooseVO;
import com.iwantjob.simulator.dto.ChoiceVO;
import com.iwantjob.simulator.dto.NodeOptionVO;
import com.iwantjob.simulator.dto.NodeVO;
import com.iwantjob.simulator.dto.ScenarioDetailVO;
import com.iwantjob.simulator.dto.ScenarioVO;
import com.iwantjob.simulator.dto.SessionHistoryVO;
import com.iwantjob.simulator.dto.SessionReportVO;
import com.iwantjob.simulator.dto.SessionStartVO;
import com.iwantjob.simulator.entity.SimulatorChoice;
import com.iwantjob.simulator.entity.SimulatorNode;
import com.iwantjob.simulator.entity.SimulatorNodeOption;
import com.iwantjob.simulator.entity.SimulatorScenario;
import com.iwantjob.simulator.entity.SimulatorSession;
import com.iwantjob.simulator.mapper.SimulatorChoiceMapper;
import com.iwantjob.simulator.mapper.SimulatorNodeMapper;
import com.iwantjob.simulator.mapper.SimulatorNodeOptionMapper;
import com.iwantjob.simulator.mapper.SimulatorScenarioMapper;
import com.iwantjob.simulator.mapper.SimulatorSessionMapper;
import com.iwantjob.simulator.service.SimulatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 职业模拟舱服务实现
 * <p>
 * 关键设计（参考开发文档 7.2 节）：
 * <ul>
 *   <li>Redis 缓存当前节点：key=sim:session:{id}，value=currentNodeId</li>
 *   <li>每个 choice 立即落 MySQL，Redis 仅缓存当前节点</li>
 *   <li>会话恢复优先读 Redis，miss 则从 MySQL 重建</li>
 *   <li>到达 is_end 节点：完成会话 + AI 生成 overall_score + 触发 BadgeTriggerEvent(SIMULATOR_COMPLETE) + PointChangeEvent(15分)</li>
 *   <li>会话归属校验：只能操作自己的会话</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SimulatorServiceImpl implements SimulatorService {

    /** 会话状态：进行中 */
    private static final int STATUS_IN_PROGRESS = 0;
    /** 会话状态：已完成 */
    private static final int STATUS_COMPLETED = 1;
    /** 会话状态：中断 */
    private static final int STATUS_INTERRUPTED = 2;

    /** 模拟舱完成奖励积分数 */
    private static final int COMPLETE_REWARD_POINTS = 15;

    /** Redis 当前节点缓存 key 前缀：sim:session:{id} */
    private static final String SESSION_NODE_KEY_PREFIX = "sim:session:";
    /** 缓存 TTL：7 天，覆盖一般演练周期 */
    private static final Duration SESSION_NODE_TTL = Duration.ofDays(7);

    private final SimulatorScenarioMapper scenarioMapper;
    private final SimulatorNodeMapper nodeMapper;
    private final SimulatorNodeOptionMapper optionMapper;
    private final SimulatorSessionMapper sessionMapper;
    private final SimulatorChoiceMapper choiceMapper;
    private final SimulatorAiGateway aiGateway;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;

    // ==================== 场景查询 ====================

    @Override
    public List<ScenarioVO> listActiveScenarios() {
        List<SimulatorScenario> list = scenarioMapper.selectList(
                new LambdaQueryWrapper<SimulatorScenario>()
                        .eq(SimulatorScenario::getIsActive, 1)
                        .orderByAsc(SimulatorScenario::getId));
        return list.stream().map(this::toScenarioVO).toList();
    }

    @Override
    public ScenarioDetailVO getScenarioDetail(Long scenarioId) {
        SimulatorScenario scenario = scenarioMapper.selectById(scenarioId);
        if (scenario == null) {
            throw new BusinessException(ErrorCode.SCENARIO_NOT_FOUND);
        }
        ScenarioDetailVO vo = new ScenarioDetailVO();
        vo.setId(scenario.getId());
        vo.setTitle(scenario.getTitle());
        vo.setType(scenario.getType());
        vo.setTypeDesc(typeDesc(scenario.getType()));
        vo.setDescription(scenario.getDescription());
        vo.setInitialContext(scenario.getInitialContext());
        vo.setDifficulty(scenario.getDifficulty());

        List<SimulatorNode> nodes = nodeMapper.selectByScenarioId(scenarioId);
        vo.setNodeCount(nodes.size());
        List<NodeVO> nodeVOs = nodes.stream().map(n -> toNodeVO(n, false)).toList();
        vo.setNodes(nodeVOs);

        // 起始节点
        if (scenario.getStartNodeId() != null) {
            for (NodeVO n : nodeVOs) {
                if (scenario.getStartNodeId().equals(n.getId())) {
                    vo.setStartNode(toNodeVO(findNode(nodes, scenario.getStartNodeId()), true));
                    break;
                }
            }
        }
        return vo;
    }

    // ==================== 会话生命周期 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SessionStartVO start(Long userId, Long scenarioId) {
        SimulatorScenario scenario = scenarioMapper.selectById(scenarioId);
        if (scenario == null || (scenario.getIsActive() == null || scenario.getIsActive() != 1)) {
            throw new BusinessException(ErrorCode.SCENARIO_NOT_FOUND, "场景不存在或已下线");
        }
        if (scenario.getStartNodeId() == null) {
            throw new BusinessException(ErrorCode.NODE_NOT_FOUND, "场景未配置起始节点");
        }

        // 创建会话：current_node_id = 场景 start_node_id
        SimulatorSession session = new SimulatorSession();
        session.setUserId(userId);
        session.setScenarioId(scenarioId);
        session.setStatus(STATUS_IN_PROGRESS);
        session.setCurrentNodeId(scenario.getStartNodeId());
        session.setStartedAt(LocalDateTime.now());
        sessionMapper.insert(session);
        log.info("创建模拟会话: sessionId={}, userId={}, scenarioId={}",
                session.getId(), userId, scenarioId);

        // 缓存当前节点到 Redis
        cacheCurrentNode(session.getId(), session.getCurrentNodeId());

        // 组装响应：起始节点描述 + 选项
        SimulatorNode startNode = nodeMapper.selectById(scenario.getStartNodeId());
        SessionStartVO vo = new SessionStartVO();
        vo.setSessionId(session.getId());
        vo.setScenarioId(scenarioId);
        vo.setScenarioTitle(scenario.getTitle());
        vo.setInitialContext(scenario.getInitialContext());
        vo.setCurrentNode(toNodeVO(startNode, true));
        vo.setStartedAt(session.getStartedAt());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChooseVO choose(Long userId, ChooseDTO dto) {
        // 1. 校验会话归属与状态
        SimulatorSession session = getOwnedSession(userId, dto.getSessionId());
        assertInProgress(session);

        // 2. 获取当前节点（优先 Redis，miss 查 MySQL 并回填）
        Long currentNodeId = resolveCurrentNode(session);
        SimulatorNode currentNode = nodeMapper.selectById(currentNodeId);
        if (currentNode == null) {
            throw new BusinessException(ErrorCode.NODE_NOT_FOUND);
        }

        // 3. 查询当前节点的选项，校验选项合法且属于当前节点
        List<SimulatorNodeOption> options = optionMapper.selectByNodeId(currentNodeId);
        SimulatorNodeOption chosen = options.stream()
                .filter(o -> dto.getOptionId().equals(o.getId()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.PARAM_ERROR, "选项不属于当前节点"));
        if (currentNode.getIsEnd() != null && currentNode.getIsEnd() == 1) {
            throw new BusinessException(ErrorCode.SESSION_ENDED, "已到达结束节点，无法继续选择");
        }

        // 4. 调 AI 生成即时反馈（@RateLimit 在 Controller 层保护）
        AiFeedback aiFeedback = aiGateway.generateScenarioFeedback(
                currentNode.getNodeDesc(),
                currentNode.getAiPromptSnippet(),
                chosen.getOptionText(),
                chosen.getSoftSkillTags());

        // 5. 写 simulator_choice（立即落 MySQL）
        SimulatorChoice choice = new SimulatorChoice();
        choice.setSessionId(session.getId());
        choice.setNodeId(currentNodeId);
        choice.setNodeDesc(currentNode.getNodeDesc());
        choice.setOptionsJson(toJson(options));
        choice.setOptionId(chosen.getId());
        choice.setUserChoice(chosen.getOptionText());
        choice.setAiFeedback(aiFeedback.getFeedback());
        choice.setSoftSkillTags(aiFeedback.getSoftSkillTags());
        choiceMapper.insert(choice);
        log.info("模拟选择落库: sessionId={}, choiceId={}, nodeId={}, optionId={}",
                session.getId(), choice.getId(), currentNodeId, chosen.getId());

        // 6. 推进 current_node_id 到 next_node_id
        Long nextNodeId = chosen.getNextNodeId();
        if (nextNodeId != null) {
            session.setCurrentNodeId(nextNodeId);
            sessionMapper.updateById(session);
            cacheCurrentNode(session.getId(), nextNodeId);
        } else {
            // next_node_id 为空：视为到达终点（兜底）
            log.warn("选项 next_node_id 为空，按结束处理: sessionId={}, optionId={}",
                    session.getId(), chosen.getId());
        }

        // 7. 判断是否到达结束节点
        boolean finished = false;
        NodeVO nextNodeVO = null;
        Integer overallScore = null;
        if (nextNodeId != null) {
            SimulatorNode nextNode = nodeMapper.selectById(nextNodeId);
            if (nextNode != null && nextNode.getIsEnd() != null && nextNode.getIsEnd() == 1) {
                // 到达结束节点：完成会话 + 生成报告 + 触发事件
                overallScore = completeSession(session, scenarioTypeOf(session.getScenarioId()));
                finished = true;
            } else if (nextNode != null) {
                nextNodeVO = toNodeVO(nextNode, true);
            }
        }

        // 8. 组装响应
        ChooseVO vo = new ChooseVO();
        vo.setSessionId(session.getId());
        vo.setChoiceId(choice.getId());
        vo.setAiFeedback(aiFeedback.getFeedback());
        vo.setSoftSkillTags(aiFeedback.getSoftSkillTags());
        vo.setFinished(finished);
        vo.setNextNode(nextNodeVO);
        vo.setStatus(session.getStatus());
        vo.setOverallScore(overallScore);
        return vo;
    }

    // ==================== 报告与历史 ====================

    @Override
    public SessionReportVO getReport(Long userId, Long sessionId) {
        SimulatorSession session = getOwnedSession(userId, sessionId);
        SimulatorScenario scenario = scenarioMapper.selectById(session.getScenarioId());

        SessionReportVO vo = new SessionReportVO();
        vo.setId(session.getId());
        vo.setScenarioId(session.getScenarioId());
        vo.setScenarioTitle(scenario == null ? null : scenario.getTitle());
        vo.setScenarioTypeDesc(scenario == null ? null : typeDesc(scenario.getType()));
        vo.setStatus(session.getStatus());
        vo.setOverallScore(session.getOverallScore());
        vo.setStartedAt(session.getStartedAt());
        vo.setCompletedAt(session.getCompletedAt());

        // 若已完成，尝试从 AI 报告还原维度评分与评价（这里基于 choices 实时复算，保证一致性）
        List<SimulatorChoice> choices = choiceMapper.selectBySessionId(sessionId);
        if (session.getStatus() != null && session.getStatus() == STATUS_COMPLETED) {
            AiReport report = aiGateway.generateReport(
                    scenario == null ? null : scenario.getType(),
                    scenario == null ? null : scenario.getDifficulty(),
                    choices);
            vo.setDimensionScores(report.getDimensionScores());
            vo.setSummary(report.getSummary());
            vo.setSuggestions(report.getSuggestions());
        }

        vo.setChoices(choices.stream().map(this::toChoiceVO).toList());
        return vo;
    }

    @Override
    public PageResult<SessionHistoryVO> mySessions(Long userId, long page, long size) {
        Page<SimulatorSession> p = new Page<>(page, size);
        Page<SimulatorSession> result = sessionMapper.selectPage(p,
                new LambdaQueryWrapper<SimulatorSession>()
                        .eq(SimulatorSession::getUserId, userId)
                        .orderByDesc(SimulatorSession::getStartedAt));

        // 批量查询场景标题与类型，避免 N+1
        Map<Long, SimulatorScenario> scenarioMap = batchGetScenarios(result.getRecords());

        List<SessionHistoryVO> records = result.getRecords().stream()
                .map(s -> toHistoryVO(s, scenarioMap))
                .toList();
        return PageResult.of(records, result.getTotal(), result.getCurrent(), result.getSize());
    }

    // ==================== 内部工具方法 ====================

    /**
     * 完成会话：AI 生成 overall_score，触发徽章与积分事件
     *
     * @return 综合得分
     */
    private Integer completeSession(SimulatorSession session, Integer scenarioType) {
        List<SimulatorChoice> choices = choiceMapper.selectBySessionId(session.getId());
        AiReport report = aiGateway.generateReport(scenarioType, null, choices);

        session.setStatus(STATUS_COMPLETED);
        session.setCompletedAt(LocalDateTime.now());
        session.setOverallScore(report.getOverallScore());
        sessionMapper.updateById(session);
        log.info("模拟会话完成: sessionId={}, overallScore={}",
                session.getId(), report.getOverallScore());

        // 清理 Redis 当前节点缓存
        evictCurrentNode(session.getId());

        // 触发徽章事件：模拟舱完成（conditionType=SIMULATOR_COMPLETE）
        eventPublisher.publishEvent(new BadgeTriggerEvent(
                session.getUserId(), BadgeCondEnum.SIMULATOR_COMPLETE, session.getId()));
        // 触发积分变更事件：完成 +15 分
        eventPublisher.publishEvent(new PointChangeEvent(
                session.getUserId(), COMPLETE_REWARD_POINTS, "模拟舱完成", session.getId()));
        return report.getOverallScore();
    }

    /**
     * 获取会话并校验归属权（只能操作自己的会话）
     */
    private SimulatorSession getOwnedSession(Long userId, Long sessionId) {
        SimulatorSession session = sessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BusinessException(ErrorCode.SESSION_NOT_FOUND);
        }
        if (!userId.equals(session.getUserId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权操作他人的模拟会话");
        }
        return session;
    }

    /**
     * 断言会话进行中
     */
    private void assertInProgress(SimulatorSession session) {
        if (session.getStatus() == null || session.getStatus() != STATUS_IN_PROGRESS) {
            throw new BusinessException(ErrorCode.SESSION_ENDED);
        }
    }

    /**
     * 解析当前节点ID：优先 Redis，miss 查 MySQL 并回填
     */
    private Long resolveCurrentNode(SimulatorSession session) {
        String key = SESSION_NODE_KEY_PREFIX + session.getId();
        String cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            try {
                return Long.parseLong(cached);
            } catch (NumberFormatException e) {
                log.warn("Redis 当前节点缓存格式异常: key={}, value={}", key, cached);
            }
        }
        // miss：从 MySQL 重建
        Long currentNodeId = session.getCurrentNodeId();
        if (currentNodeId != null) {
            cacheCurrentNode(session.getId(), currentNodeId);
        }
        return currentNodeId;
    }

    private void cacheCurrentNode(Long sessionId, Long nodeId) {
        if (nodeId == null) return;
        redisTemplate.opsForValue().set(SESSION_NODE_KEY_PREFIX + sessionId,
                String.valueOf(nodeId), SESSION_NODE_TTL);
    }

    private void evictCurrentNode(Long sessionId) {
        redisTemplate.delete(SESSION_NODE_KEY_PREFIX + sessionId);
    }

    /**
     * 查询会话对应场景的类型
     */
    private Integer scenarioTypeOf(Long scenarioId) {
        if (scenarioId == null) return null;
        SimulatorScenario s = scenarioMapper.selectById(scenarioId);
        return s == null ? null : s.getType();
    }

    /**
     * 批量查询会话涉及的场景，避免 N+1
     */
    private Map<Long, SimulatorScenario> batchGetScenarios(List<SimulatorSession> sessions) {
        if (sessions == null || sessions.isEmpty()) return Map.of();
        List<Long> ids = sessions.stream()
                .map(SimulatorSession::getScenarioId)
                .distinct()
                .toList();
        if (ids.isEmpty()) return Map.of();
        List<SimulatorScenario> list = scenarioMapper.selectBatchIds(ids);
        return list.stream().collect(Collectors.toMap(SimulatorScenario::getId, s -> s));
    }

    private SimulatorNode findNode(List<SimulatorNode> nodes, Long id) {
        return nodes.stream().filter(n -> id.equals(n.getId())).findFirst().orElse(null);
    }

    private String typeDesc(Integer type) {
        if (type == null) return null;
        return switch (type) {
            case 0 -> "入职";
            case 1 -> "向上汇报";
            case 2 -> "冲突处理";
            case 3 -> "跨部门协作";
            default -> null;
        };
    }

    // ==================== 实体转 VO ====================

    private ScenarioVO toScenarioVO(SimulatorScenario s) {
        ScenarioVO vo = new ScenarioVO();
        vo.setId(s.getId());
        vo.setTitle(s.getTitle());
        vo.setType(s.getType());
        vo.setTypeDesc(typeDesc(s.getType()));
        vo.setDescription(s.getDescription());
        vo.setDifficulty(s.getDifficulty());
        vo.setCreatedAt(s.getCreatedAt());
        return vo;
    }

    /**
     * 节点转 VO
     *
     * @param withOptions 是否携带选项（详情/起始节点携带；列表可视化可省略）
     */
    private NodeVO toNodeVO(SimulatorNode n, boolean withOptions) {
        if (n == null) return null;
        NodeVO vo = new NodeVO();
        vo.setId(n.getId());
        vo.setScenarioId(n.getScenarioId());
        vo.setNodeDesc(n.getNodeDesc());
        vo.setIsEnd(n.getIsEnd());
        vo.setSortOrder(n.getSortOrder());
        if (withOptions) {
            List<SimulatorNodeOption> options = optionMapper.selectByNodeId(n.getId());
            vo.setOptions(options.stream().map(this::toOptionVO).toList());
        }
        return vo;
    }

    private NodeOptionVO toOptionVO(SimulatorNodeOption o) {
        NodeOptionVO vo = new NodeOptionVO();
        vo.setId(o.getId());
        vo.setNodeId(o.getNodeId());
        vo.setOptionText(o.getOptionText());
        vo.setSoftSkillTags(o.getSoftSkillTags());
        return vo;
    }

    private ChoiceVO toChoiceVO(SimulatorChoice c) {
        ChoiceVO vo = new ChoiceVO();
        vo.setId(c.getId());
        vo.setNodeId(c.getNodeId());
        vo.setNodeDesc(c.getNodeDesc());
        vo.setOptionId(c.getOptionId());
        vo.setUserChoice(c.getUserChoice());
        vo.setAiFeedback(c.getAiFeedback());
        vo.setSoftSkillTags(c.getSoftSkillTags());
        vo.setCreatedAt(c.getCreatedAt());
        return vo;
    }

    private SessionHistoryVO toHistoryVO(SimulatorSession s, Map<Long, SimulatorScenario> scenarioMap) {
        SessionHistoryVO vo = new SessionHistoryVO();
        vo.setId(s.getId());
        vo.setScenarioId(s.getScenarioId());
        SimulatorScenario scenario = scenarioMap.get(s.getScenarioId());
        vo.setScenarioTitle(scenario == null ? null : scenario.getTitle());
        vo.setScenarioTypeDesc(scenario == null ? null : typeDesc(scenario.getType()));
        vo.setStatus(s.getStatus());
        vo.setOverallScore(s.getOverallScore());
        vo.setStartedAt(s.getStartedAt());
        vo.setCompletedAt(s.getCompletedAt());
        return vo;
    }

    // ==================== JSON 工具 ====================

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("序列化选项快照失败: {}", e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "选项快照序列化失败");
        }
    }

    /** 备用：解析 options_json（当前实现未直接使用，保留供后续报告展开） */
    @SuppressWarnings("unused")
    private List<NodeOptionVO> parseOptions(String json) {
        if (json == null || json.isBlank()) return new ArrayList<>();
        try {
            return objectMapper.readValue(json, new TypeReference<List<NodeOptionVO>>() {});
        } catch (Exception e) {
            log.warn("解析 options_json 失败: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
}
