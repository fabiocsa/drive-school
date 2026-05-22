package com.driveschool.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.driveschool.entity.Appointment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AppointmentMapper extends BaseMapper<Appointment> {
}
