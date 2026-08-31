package com.iwantjob.simulator.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iwantjob.simulator.entity.SimulatorSession;
import org.apache.ibatis.annotations.Mapper;

/**
 * 模拟舱会话 Mapper
 */
@Mapper
public interface SimulatorSessionMapper extends BaseMapper<SimulatorSession> {
}
