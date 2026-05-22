package com.driveschool.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.driveschool.entity.*;
import com.driveschool.mapper.*;
import com.driveschool.service.StatisticsService;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    private final StudentInfoMapper studentInfoMapper;
    private final ExamRegistrationMapper examRegistrationMapper;
    private final TrainingHourMapper trainingHourMapper;
    private final CoachMapper coachMapper;
    private final UserMapper userMapper;

    public StatisticsServiceImpl(StudentInfoMapper studentInfoMapper,
                                  ExamRegistrationMapper examRegistrationMapper,
                                  TrainingHourMapper trainingHourMapper,
                                  CoachMapper coachMapper,
                                  UserMapper userMapper) {
        this.studentInfoMapper = studentInfoMapper;
        this.examRegistrationMapper = examRegistrationMapper;
        this.trainingHourMapper = trainingHourMapper;
        this.coachMapper = coachMapper;
        this.userMapper = userMapper;
    }

    @Override
    public Map<String, Object> getRegistrationStats(int year, Integer month) {
        Map<String, Object> result = new HashMap<>();
        if (month != null && month > 0) {
            java.util.List<Long> dailyCounts = new ArrayList<>();
            java.util.List<String> labels = new ArrayList<>();
            int daysInMonth = java.time.YearMonth.of(year, month).lengthOfMonth();
            for (int d = 1; d <= daysInMonth; d++) {
                Long count = studentInfoMapper.countByDay(year, month, d);
                dailyCounts.add(count != null ? count : 0L);
                labels.add(month + "/" + d);
            }
            result.put("labels", labels);
            result.put("data", dailyCounts);
        } else {
            java.util.List<Long> monthlyCounts = new ArrayList<>();
            java.util.List<String> labels = new ArrayList<>();
            for (int m = 1; m <= 12; m++) {
                Long count = studentInfoMapper.countByMonth(year, m);
                monthlyCounts.add(count != null ? count : 0L);
                labels.add(year + "-" + (m < 10 ? "0" + m : m));
            }
            result.put("labels", labels);
            result.put("data", monthlyCounts);
        }
        return result;
    }

    @Override
    public Map<String, Object> getPassRateStats() {
        java.util.List<Map<String, Object>> rawData = examRegistrationMapper.countPassRateBySubject();
        Map<String, Object> result = new HashMap<>();
        java.util.List<String> labels = new ArrayList<>();
        java.util.List<Double> rates = new ArrayList<>();

        for (Map<String, Object> row : rawData) {
            Long subjectId = (Long) row.get("subject_id");
            long total = ((Number) row.get("total")).longValue();
            long passed = ((Number) row.get("passed")).longValue();
            String subjectName = "科目" + subjectId;
            labels.add(subjectName);
            rates.add(total > 0 ? (double) passed / total * 100.0 : 0.0);
        }
        result.put("labels", labels);
        result.put("data", rates);
        return result;
    }

    @Override
    public Map<String, Object> getCoachWorkloadStats() {
        java.util.List<Coach> coaches = coachMapper.selectList(null);
        Map<String, Object> result = new HashMap<>();
        java.util.List<String> labels = new ArrayList<>();
        java.util.List<Long> studentCounts = new ArrayList<>();
        java.util.List<java.math.BigDecimal> totalHours = new ArrayList<>();

        for (Coach coach : coaches) {
            User user = userMapper.selectById(coach.getUserId());
            labels.add(user != null ? user.getRealName() : "教练" + coach.getId());
            Long count = studentInfoMapper.countByCoachId(coach.getId());
            studentCounts.add(count != null ? count : 0L);
            java.math.BigDecimal hours = trainingHourMapper.sumDurationByCoachId(coach.getId());
            totalHours.add(hours != null ? hours : java.math.BigDecimal.ZERO);
        }
        result.put("labels", labels);
        result.put("studentCounts", studentCounts);
        result.put("totalHours", totalHours);
        return result;
    }
}
