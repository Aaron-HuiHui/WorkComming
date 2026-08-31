package com.iwantjob.simulator.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iwantjob.simulator.entity.SimulatorNodeOption;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 模拟舱节点选项 Mapper
 */
@Mapper
public interface SimulatorNodeOptionMapper extends BaseMapper<SimulatorNodeOption> {

    /**
     * 按节点ID查询全部选项
     */
    @Select("SELECT * FROM simulator_node_option WHERE node_id = #{nodeId}")
    List<SimulatorNodeOption> selectByNodeId(@Param("nodeId") Long nodeId);
}
