package com.iwantjob.simulator.service;

import com.iwantjob.common.result.PageResult;
import com.iwantjob.simulator.dto.ChooseDTO;
import com.iwantjob.simulator.dto.ChooseVO;
import com.iwantjob.simulator.dto.ScenarioDetailVO;
import com.iwantjob.simulator.dto.ScenarioVO;
import com.iwantjob.simulator.dto.SessionHistoryVO;
import com.iwantjob.simulator.dto.SessionReportVO;
import com.iwantjob.simulator.dto.SessionStartVO;

import java.util.List;

/**
 * 职业模拟舱服务
 */
public interface SimulatorService {

    /**
     * 可用场景列表（is_active=1）
     */
    List<ScenarioVO> listActiveScenarios();

    /**
     * 场景详情（含起始节点）
     */
    ScenarioDetailVO getScenarioDetail(Long scenarioId);

    /**
     * 开始模拟会话：current_node_id 置为场景 start_node_id，返回起始节点描述+选项
     */
    SessionStartVO start(Long userId, Long scenarioId);

    /**
     * 提交选择：写 simulator_choice → 调 AI 生成反馈 → 推进 current_node_id；
     * 到达 is_end 节点则完成会话 + 生成 overall_score + 触发徽章/积分事件
     */
    ChooseVO choose(Long userId, ChooseDTO dto);

    /**
     * 会话报告（含所有 choice + 评分）
     */
    SessionReportVO getReport(Long userId, Long sessionId);

    /**
     * 我的模拟历史（分页）
     */
    PageResult<SessionHistoryVO> mySessions(Long userId, long page, long size);
}
