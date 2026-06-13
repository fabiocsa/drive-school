package com.driveschool.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.driveschool.entity.*;
import com.driveschool.mapper.*;
import com.driveschool.service.ExamRegistrationService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
public class ExamRegistrationServiceImpl extends ServiceImpl<ExamRegistrationMapper, ExamRegistration>
        implements ExamRegistrationService {

    private final StudentInfoMapper studentInfoMapper;
    private final LearningPhaseMapper learningPhaseMapper;
    private final UserMapper userMapper;
    private final ExamLocationMapper examLocationMapper;

    public ExamRegistrationServiceImpl(StudentInfoMapper studentInfoMapper,
                                        LearningPhaseMapper learningPhaseMapper,
                                        UserMapper userMapper,
                                        ExamLocationMapper examLocationMapper) {
        this.studentInfoMapper = studentInfoMapper;
        this.learningPhaseMapper = learningPhaseMapper;
        this.userMapper = userMapper;
        this.examLocationMapper = examLocationMapper;
    }

    @Override
    public ExamRegistration registerExam(Long userId, Map<String, Object> data) {
        StudentInfo info = studentInfoMapper.selectOne(
                new LambdaQueryWrapper<StudentInfo>().eq(StudentInfo::getUserId, userId));
        if (info == null) throw new RuntimeException("请先完成报名");

        LearningPhase phase = learningPhaseMapper.selectOne(
                new LambdaQueryWrapper<LearningPhase>().eq(LearningPhase::getStudentId, info.getId()));
        if (phase == null) throw new RuntimeException("学习阶段信息不存在");

        Long subjectId = Long.valueOf(data.get("subjectId").toString());
        int subjectNum = subjectId.intValue();

        if (subjectNum == 1 && phase.getPhase1Completed() == 0)
            throw new RuntimeException("科目一学时未达标");
        if (subjectNum == 2 && phase.getPhase2Completed() == 0)
            throw new RuntimeException("科目二学时未达标");
        if (subjectNum == 3 && phase.getPhase3Completed() == 0)
            throw new RuntimeException("科目三学时未达标");
        if (subjectNum == 4 && phase.getPhase4Completed() == 0)
            throw new RuntimeException("科目四学时未达标");

        if (subjectNum >= 2) {
            ExamRegistration prev = getOne(new LambdaQueryWrapper<ExamRegistration>()
                    .eq(ExamRegistration::getStudentId, info.getId())
                    .eq(ExamRegistration::getSubjectId, subjectNum - 1)
                    .eq(ExamRegistration::getIsPassed, 1));
            if (prev == null)
                throw new RuntimeException("请先通过上一科目考试");
        }

        int retryCount = (int) count(new LambdaQueryWrapper<ExamRegistration>()
                .eq(ExamRegistration::getStudentId, info.getId())
                .eq(ExamRegistration::getSubjectId, subjectId));
        if (retryCount >= 5) throw new RuntimeException("该科目补考次数已达上限");

        // 考场容量校验：该考场 + 日期的已批准考试数 < 考场容量
        Long locId = Long.valueOf(data.get("examLocationId").toString());
        LocalDate examDate = LocalDate.parse(data.get("examDate").toString());
        ExamLocation location = examLocationMapper.selectById(locId);
        if (location == null) throw new RuntimeException("考场不存在");
        long bookedCount = count(new LambdaQueryWrapper<ExamRegistration>()
                .eq(ExamRegistration::getExamLocationId, locId)
                .eq(ExamRegistration::getExamDate, examDate)
                .eq(ExamRegistration::getStatus, "APPROVED"));
        if (bookedCount >= location.getCapacity()) {
            throw new RuntimeException("该考场该日期已约满（容量" + location.getCapacity() + "），请选择其他日期或考场");
        }

        ExamRegistration exam = new ExamRegistration();
        exam.setStudentId(info.getId());
        exam.setSubjectId(subjectId);
        exam.setExamLocationId(locId);
        exam.setExamDate(examDate);
        exam.setStatus("PENDING");
        exam.setRetryCount(retryCount);
        save(exam);
        return exam;
    }

    @Override
    public void adminAuditExam(Long examId, String status) {
        ExamRegistration exam = getById(examId);
        if (exam == null) throw new RuntimeException("考试报名不存在");
        if ("APPROVED".equals(status)) {
            exam.setStatus("APPROVED");
        } else if ("REJECTED".equals(status)) {
            exam.setStatus("REJECTED");
        }
        updateById(exam);
    }

    @Override
    public void recordScore(Long examId, BigDecimal score, Integer isPassed) {
        ExamRegistration exam = getById(examId);
        if (exam == null) throw new RuntimeException("考试报名不存在");
        exam.setScore(score);
        exam.setIsPassed(isPassed);
        exam.setStatus("COMPLETED");
        updateById(exam);

        if (isPassed == 1) {
            checkAndUpdateCertStatus(exam.getStudentId());
        }
    }

    @Override
    public List<ExamRegistration> getStudentExams(Long userId) {
        StudentInfo info = studentInfoMapper.selectOne(
                new LambdaQueryWrapper<StudentInfo>().eq(StudentInfo::getUserId, userId));
        if (info == null) return Collections.emptyList();
        return list(new LambdaQueryWrapper<ExamRegistration>()
                .eq(ExamRegistration::getStudentId, info.getId())
                .orderByDesc(ExamRegistration::getCreatedTime));
    }

    @Override
    public void checkAndUpdateCertStatus(Long studentId) {
        long completedCount = count(new LambdaQueryWrapper<ExamRegistration>()
                .eq(ExamRegistration::getStudentId, studentId)
                .eq(ExamRegistration::getIsPassed, 1));
        if (completedCount >= 4) {
            StudentInfo info = studentInfoMapper.selectById(studentId);
            if (info != null) {
                info.setCertStatus("WAITING");
                studentInfoMapper.updateById(info);
            }
        }
    }

    @Override
    public List<Map<String, Object>> getWaitingCertStudents() {
        List<StudentInfo> students = studentInfoMapper.selectList(
                new LambdaQueryWrapper<StudentInfo>().eq(StudentInfo::getCertStatus, "WAITING"));
        List<Map<String, Object>> result = new ArrayList<>();
        for (StudentInfo si : students) {
            Map<String, Object> map = new HashMap<>();
            map.put("studentInfo", si);
            User user = userMapper.selectById(si.getUserId());
            map.put("realName", user != null ? user.getRealName() : "");
            map.put("phone", user != null ? user.getPhone() : "");
            result.add(map);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> listAllExams() {
        List<ExamRegistration> exams = list(new LambdaQueryWrapper<ExamRegistration>()
                .orderByDesc(ExamRegistration::getCreatedTime));
        List<Map<String, Object>> result = new ArrayList<>();
        for (ExamRegistration exam : exams) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", exam.getId());
            map.put("studentId", exam.getStudentId());
            map.put("subjectId", exam.getSubjectId());
            map.put("examLocationId", exam.getExamLocationId());
            map.put("examDate", exam.getExamDate());
            map.put("status", exam.getStatus());
            map.put("score", exam.getScore());
            map.put("isPassed", exam.getIsPassed());
            map.put("retryCount", exam.getRetryCount());
            map.put("createdTime", exam.getCreatedTime());
            // 关联学员姓名
            StudentInfo si = studentInfoMapper.selectById(exam.getStudentId());
            if (si != null) {
                User user = userMapper.selectById(si.getUserId());
                map.put("studentName", user != null ? user.getRealName() : "");
                map.put("studentCard", si.getIdCard());
            } else {
                map.put("studentName", "");
                map.put("studentCard", "");
            }
            result.add(map);
        }
        return result;
    }

    @Override
    public void issueCert(Long studentId) {
        StudentInfo info = studentInfoMapper.selectById(studentId);
        if (info == null) throw new RuntimeException("学员不存在");
        info.setCertStatus("ISSUED");
        studentInfoMapper.updateById(info);
    }
}
