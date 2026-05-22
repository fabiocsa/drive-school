package com.driveschool.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.driveschool.entity.ExamRegistration;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface ExamRegistrationMapper extends BaseMapper<ExamRegistration> {

    @Select("SELECT subject_id, COUNT(*) as total, SUM(CASE WHEN is_passed = 1 THEN 1 ELSE 0 END) as passed " +
            "FROM exam_registration WHERE status = 'COMPLETED' GROUP BY subject_id")
    List<Map<String, Object>> countPassRateBySubject();
}
