package com.driveschool.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.driveschool.entity.*;
import com.driveschool.mapper.*;
import com.driveschool.service.TrainingHourService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
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
    @Transactional
    public TrainingHour record(TrainingHour hour) {
        // 默认记录日期为当天
        if (hour.getRecordDate() == null) {
            hour.setRecordDate(LocalDate.now());
        }
        save(hour);

        // 将学时累计到当前阶段
        accumulatePhaseHours(hour.getStudentId(), hour.getDuration());
        // 检查并更新阶段
        checkAndUpdatePhase(hour.getStudentId());
        return hour;
    }

    /**
     * 将学时累计到学员当前学习阶段
     */
    private void accumulatePhaseHours(Long studentId, BigDecimal duration) {
        LearningPhase phase = learningPhaseMapper.selectOne(
                new LambdaQueryWrapper<LearningPhase>().eq(LearningPhase::getStudentId, studentId));
        if (phase == null) return;

        String current = phase.getCurrentPhase();
        if ("COMPLETED".equals(current)) return;

        BigDecimal hours = duration != null ? duration : BigDecimal.ZERO;
        switch (current) {
            case "PHASE1":
                phase.setPhase1Hours(safeAdd(phase.getPhase1Hours(), hours));
                break;
            case "PHASE2":
                phase.setPhase2Hours(safeAdd(phase.getPhase2Hours(), hours));
                break;
            case "PHASE3":
                phase.setPhase3Hours(safeAdd(phase.getPhase3Hours(), hours));
                break;
            case "PHASE4":
                phase.setPhase4Hours(safeAdd(phase.getPhase4Hours(), hours));
                break;
        }
        learningPhaseMapper.updateById(phase);
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
    @Transactional
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

        boolean updated = false;

        // 阶段1：检查阶段1累计学时是否达标
        if (phase.getPhase1Completed() == 0 &&
                phase.getPhase1Hours().compareTo(BigDecimal.valueOf(requiredHours.getOrDefault("phase1", 12))) >= 0) {
            phase.setPhase1Completed(1);
            phase.setCurrentPhase("PHASE2");
            updated = true;
        }

        // 阶段2：检查阶段2累计学时是否达标
        if (phase.getPhase1Completed() == 1 && phase.getPhase2Completed() == 0 &&
                phase.getPhase2Hours().compareTo(BigDecimal.valueOf(requiredHours.getOrDefault("phase2", 16))) >= 0) {
            phase.setPhase2Completed(1);
            phase.setCurrentPhase("PHASE3");
            updated = true;
        }

        // 阶段3：检查阶段3累计学时是否达标
        if (phase.getPhase2Completed() == 1 && phase.getPhase3Completed() == 0 &&
                phase.getPhase3Hours().compareTo(BigDecimal.valueOf(requiredHours.getOrDefault("phase3", 24))) >= 0) {
            phase.setPhase3Completed(1);
            phase.setCurrentPhase("PHASE4");
            updated = true;
        }

        // 阶段4：检查阶段4累计学时是否达标
        if (phase.getPhase3Completed() == 1 && phase.getPhase4Completed() == 0 &&
                phase.getPhase4Hours().compareTo(BigDecimal.valueOf(requiredHours.getOrDefault("phase4", 10))) >= 0) {
            phase.setPhase4Completed(1);
            phase.setCurrentPhase("COMPLETED");
            updated = true;
        }

        if (updated) {
            learningPhaseMapper.updateById(phase);
        }
    }

    @Override
    @Transactional
    public void manualAdjustPhase(Long studentId, String newPhase) {
        LearningPhase phase = learningPhaseMapper.selectOne(
                new LambdaQueryWrapper<LearningPhase>().eq(LearningPhase::getStudentId, studentId));
        if (phase == null) throw new RuntimeException("学习阶段信息不存在");

        switch (newPhase) {
            case "PHASE1":
                phase.setCurrentPhase("PHASE1");
                phase.setPhase1Completed(0);
                break;
            case "PHASE2":
                phase.setCurrentPhase("PHASE2");
                phase.setPhase1Completed(1);
                break;
            case "PHASE3":
                phase.setCurrentPhase("PHASE3");
                phase.setPhase1Completed(1);
                phase.setPhase2Completed(1);
                break;
            case "PHASE4":
                phase.setCurrentPhase("PHASE4");
                phase.setPhase1Completed(1);
                phase.setPhase2Completed(1);
                phase.setPhase3Completed(1);
                break;
            case "COMPLETED":
                phase.setCurrentPhase("COMPLETED");
                phase.setPhase1Completed(1);
                phase.setPhase2Completed(1);
                phase.setPhase3Completed(1);
                phase.setPhase4Completed(1);
                break;
            default:
                throw new RuntimeException("无效的阶段参数");
        }
        learningPhaseMapper.updateById(phase);
    }

    private BigDecimal safeAdd(BigDecimal a, BigDecimal b) {
        if (a == null) a = BigDecimal.ZERO;
        if (b == null) b = BigDecimal.ZERO;
        return a.add(b);
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
