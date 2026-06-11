package com.driveschool.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.driveschool.entity.TrainingHour;

import java.math.BigDecimal;
import java.util.Map;

public interface TrainingHourService extends IService<TrainingHour> {
    TrainingHour record(TrainingHour hour);
    BigDecimal getTotalHours(Long studentId);
    java.util.List<TrainingHour> getStudentRecords(Long studentId);
    void checkAndUpdatePhase(Long studentId);
    void manualAdjustPhase(Long studentId, String newPhase);
}
