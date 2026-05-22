package com.driveschool.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.driveschool.entity.TrainingHour;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

@Mapper
public interface TrainingHourMapper extends BaseMapper<TrainingHour> {

    @Select("SELECT COALESCE(SUM(duration), 0) FROM training_hour WHERE student_id = #{studentId}")
    BigDecimal sumDurationByStudentId(@Param("studentId") Long studentId);

    @Select("SELECT COALESCE(SUM(duration), 0) FROM training_hour WHERE coach_id = #{coachId}")
    BigDecimal sumDurationByCoachId(@Param("coachId") Long coachId);
}
