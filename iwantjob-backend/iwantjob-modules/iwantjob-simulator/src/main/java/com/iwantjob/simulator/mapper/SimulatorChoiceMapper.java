package com.iwantjob.simulator.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iwantjob.simulator.entity.SimulatorChoice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 模拟舱选择记录 Mapper
 */
@Mapper
public interface SimulatorChoiceMapper extends BaseMapper<SimulatorChoice> {

    /**
     * 按会话ID查询全部选择记录，按 created_at 升序（即演练时间顺序）
     */
    @Select("SELECT * FROM simulator_choice WHERE session_id = #{sessionId} ORDER BY created_at ASC")
    List<SimulatorChoice> selectBySessionId(@Param("sessionId") Long sessionId);
}
