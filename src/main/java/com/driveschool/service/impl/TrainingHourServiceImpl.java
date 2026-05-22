package com.driveschool.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.driveschool.entity.*;
import com.driveschool.mapper.*;
import com.driveschool.service.TrainingHourService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class TrainingHourServiceImpl extends ServiceImpl<TrainingHourMapper, TrainingHour> implements TrainingHourService {

    private final LearningPhaseMapper learningPhaseMapper;
    private final StudentInfoMapper studentInfoMapper;
    private final VehicleTypeMapper vehicleTypeMapper;

    public TrainingHourServiceImpl(LearningPhaseMapper learningPhaseMapper,
                                    StudentInfoMapper studentInfoMapper,
                                    VehicleTypeMapper vehicleTypeMapper) {
        this.learningPhaseMapper = learningPhaseMapper;
        this.studentInfoMapper = studentInfoMapper;
        this.vehicleTypeMapper = vehicleTypeMapper;
    }

    @Override
    public TrainingHour record(TrainingHour hour) {
        save(hour);
        checkAndUpdatePhase(hour.getStudentId());
        return hour;
    }

    @Override
    public BigDecimal getTotalHours(Long studentId) {
        BigDecimal sum = baseMapper.sumDurationByStudentId(studentId);
        return sum != null ? sum : BigDecimal.ZERO;
    }

    @Override
    public List<TrainingHour> getStudentRecords(Long studentId) {
        return list(new LambdaQueryWrapper<TrainingHour>()
                .eq(TrainingHour::getStudentId, studentId)
                .orderByDesc(TrainingHour::getRecordDate));
    }

    @Override
    public void checkAndUpdatePhase(Long studentId) {
        LearningPhase phase = learningPhaseMapper.selectOne(
                new LambdaQueryWrapper<LearningPhase>().eq(LearningPhase::getStudentId, studentId));
        if (phase == null || "COMPLETED".equals(phase.getCurrentPhase())) return;

        StudentInfo info = studentInfoMapper.selectById(studentId);
        if (info == null) return;

        VehicleType vt = vehicleTypeMapper.selectById(info.getVehicleTypeId());
        if (vt == null) return;

        Map<String, Integer> requiredHours = parseHoursJson(vt.getSubjectHoursJson());
        if (requiredHours == null) return;

        BigDecimal total = getTotalHours(studentId);

        if (total.compareTo(BigDecimal.valueOf(requiredHours.getOrDefault("phase1", 12))) >= 0
                && phase.getPhase1Completed() == 0) {
            phase.setPhase1Completed(1);
            phase.setCurrentPhase("PHASE2");
        }

        BigDecimal phase2Total = phase.getPhase1Hours().add(phase.getPhase2Hours());
        if (phase2Total.compareTo(BigDecimal.valueOf(requiredHours.getOrDefault("phase2", 16))) >= 0) {
            phase.setPhase2Completed(1);
            phase.setCurrentPhase("PHASE3");
        }

        BigDecimal phase3Total = phase.getPhase2Hours().add(phase.getPhase3Hours());
        if (phase3Total.compareTo(BigDecimal.valueOf(requiredHours.getOrDefault("phase3", 24))) >= 0) {
            phase.setPhase3Completed(1);
            phase.setCurrentPhase("PHASE4");
        }

        BigDecimal phase4Total = phase.getPhase3Hours().add(phase.getPhase4Hours());
        if (phase4Total.compareTo(BigDecimal.valueOf(requiredHours.getOrDefault("phase4", 10))) >= 0) {
            phase.setPhase4Completed(1);
            phase.setCurrentPhase("COMPLETED");
        }

        learningPhaseMapper.updateById(phase);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Integer> parseHoursJson(String json) {
        if (json == null) return null;
        try {
            return new ObjectMapper().readValue(json, Map.class);
        } catch (Exception e) {
            return null;
        }
    }
}
