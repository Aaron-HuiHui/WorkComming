package com.iwantjob.simulator.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iwantjob.simulator.entity.SimulatorScenario;
import org.apache.ibatis.annotations.Mapper;

/**
 * 模拟舱场景 Mapper
 */
@Mapper
public interface SimulatorScenarioMapper extends BaseMapper<SimulatorScenario> {
}
