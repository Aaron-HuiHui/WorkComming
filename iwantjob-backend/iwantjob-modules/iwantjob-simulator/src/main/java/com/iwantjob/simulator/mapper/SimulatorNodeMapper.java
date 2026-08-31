package com.iwantjob.simulator.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iwantjob.simulator.entity.SimulatorNode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 模拟舱节点 Mapper
 */
@Mapper
public interface SimulatorNodeMapper extends BaseMapper<SimulatorNode> {

    /**
     * 按场景ID查询全部节点，按 sort_order 升序
     */
    @Select("SELECT * FROM simulator_node WHERE scenario_id = #{scenarioId} ORDER BY sort_order ASC")
    List<SimulatorNode> selectByScenarioId(@Param("scenarioId") Long scenarioId);
}
