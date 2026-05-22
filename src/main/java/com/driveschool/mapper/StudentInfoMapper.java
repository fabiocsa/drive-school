package com.driveschool.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.driveschool.entity.StudentInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface StudentInfoMapper extends BaseMapper<StudentInfo> {

    @Select("SELECT COUNT(*) FROM student_info WHERE YEAR(registration_time) = #{year} AND MONTH(registration_time) = #{month}")
    Long countByMonth(@Param("year") int year, @Param("month") int month);

    @Select("SELECT COUNT(*) FROM student_info WHERE YEAR(registration_time) = #{year} AND MONTH(registration_time) = #{month} AND DAY(registration_time) = #{day}")
    Long countByDay(@Param("year") int year, @Param("month") int month, @Param("day") int day);

    @Select("SELECT COUNT(*) FROM student_info WHERE coach_id = #{coachId}")
    Long countByCoachId(@Param("coachId") Long coachId);
}
