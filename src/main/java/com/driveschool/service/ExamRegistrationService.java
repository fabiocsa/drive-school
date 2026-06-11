package com.driveschool.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.driveschool.entity.ExamRegistration;
import com.driveschool.entity.LearningPhase;

import java.util.List;
import java.util.Map;

public interface ExamRegistrationService extends IService<ExamRegistration> {
    ExamRegistration registerExam(Long studentId, Map<String, Object> data);
    void adminAuditExam(Long examId, String status);
    void recordScore(Long examId, java.math.BigDecimal score, Integer isPassed);
    List<ExamRegistration> getStudentExams(Long studentId);
    void checkAndUpdateCertStatus(Long studentId);
    java.util.List<Map<String, Object>> getWaitingCertStudents();
    java.util.List<Map<String, Object>> listAllExams();
    void issueCert(Long studentId);
}
