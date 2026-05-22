package com.driveschool.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.driveschool.entity.*;
import com.driveschool.mapper.*;
import com.driveschool.service.*;
import com.driveschool.util.Result;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final StudentInfoService studentInfoService;
    private final CoachService coachService;
    private final TrainingHourService trainingHourService;
    private final AppointmentService appointmentService;
    private final ExamRegistrationService examRegistrationService;
    private final StatisticsService statisticsService;
    private final StudentInfoMapper studentInfoMapper;
    private final VehicleTypeMapper vehicleTypeMapper;
    private final SubjectMapper subjectMapper;
    private final ExamLocationMapper examLocationMapper;
    private final FeeStandardMapper feeStandardMapper;
    private final UserMapper userMapper;
    private final CoachMapper coachMapper;

    public AdminController(StudentInfoService studentInfoService, CoachService coachService,
                            TrainingHourService trainingHourService, AppointmentService appointmentService,
                            ExamRegistrationService examRegistrationService, StatisticsService statisticsService,
                            StudentInfoMapper studentInfoMapper, VehicleTypeMapper vehicleTypeMapper,
                            SubjectMapper subjectMapper, ExamLocationMapper examLocationMapper,
                            FeeStandardMapper feeStandardMapper, UserMapper userMapper, CoachMapper coachMapper) {
        this.studentInfoService = studentInfoService;
        this.coachService = coachService;
        this.trainingHourService = trainingHourService;
        this.appointmentService = appointmentService;
        this.examRegistrationService = examRegistrationService;
        this.statisticsService = statisticsService;
        this.studentInfoMapper = studentInfoMapper;
        this.vehicleTypeMapper = vehicleTypeMapper;
        this.subjectMapper = subjectMapper;
        this.examLocationMapper = examLocationMapper;
        this.feeStandardMapper = feeStandardMapper;
        this.userMapper = userMapper;
        this.coachMapper = coachMapper;
    }

    @GetMapping("/students")
    public Result<?> listStudents() {
        return Result.ok(studentInfoMapper.selectList(null));
    }

    @PutMapping("/students/audit/{id}")
    public Result<?> auditStudent(@PathVariable Long id, @RequestBody Map<String, String> data) {
        studentInfoService.adminAudit(id, data.get("status"), data.get("remark"));
        return Result.ok();
    }

    @GetMapping("/students/{id}/recommend-coaches")
    public Result<?> recommendCoaches(@PathVariable Long id) {
        StudentInfo info = studentInfoMapper.selectById(id);
        if (info == null) return Result.fail("学员不存在");
        return Result.ok(coachService.recommendCoaches(info.getVehicleTypeId()));
    }

    @PutMapping("/students/{id}/assign-coach")
    public Result<?> assignCoach(@PathVariable Long id, @RequestBody Map<String, Long> data) {
        studentInfoService.assignCoach(id, data.get("coachId"));
        return Result.ok();
    }

    @GetMapping("/coaches")
    public Result<?> listCoaches() {
        return Result.ok(coachService.listWithDetails());
    }

    @PostMapping("/coaches")
    public Result<?> addCoach(@RequestBody Map<String, Object> data) {
        User user = new User();
        user.setUsername((String) data.get("username"));
        user.setPassword(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder()
                .encode((String) data.getOrDefault("password", "123456")));
        user.setRole("ROLE_COACH");
        user.setRealName((String) data.get("realName"));
        user.setPhone((String) data.get("phone"));
        user.setEnabled(1);
        userMapper.insert(user);
        Coach coach = new Coach();
        coach.setUserId(user.getId());
        coach.setGender((String) data.get("gender"));
        coach.setVehicleTypeId(Long.valueOf(data.get("vehicleTypeId").toString()));
        coach.setRating((Integer) data.getOrDefault("rating", 3));
        coach.setScheduleJson((String) data.get("scheduleJson"));
        coachMapper.insert(coach);
        return Result.ok();
    }

    @PutMapping("/coaches/{id}")
    public Result<?> updateCoach(@PathVariable Long id, @RequestBody Map<String, Object> data) {
        Coach coach = coachMapper.selectById(id);
        if (coach == null) return Result.fail("教练不存在");
        if (data.containsKey("vehicleTypeId")) coach.setVehicleTypeId(Long.valueOf(data.get("vehicleTypeId").toString()));
        if (data.containsKey("rating")) coach.setRating((Integer) data.get("rating"));
        if (data.containsKey("scheduleJson")) coach.setScheduleJson((String) data.get("scheduleJson"));
        if (data.containsKey("gender")) coach.setGender((String) data.get("gender"));
        coachMapper.updateById(coach);
        return Result.ok();
    }

    @DeleteMapping("/coaches/{id}")
    public Result<?> deleteCoach(@PathVariable Long id) {
        coachMapper.deleteById(id);
        return Result.ok();
    }

    @GetMapping("/vehicle-types")
    public Result<?> listVehicleTypes() {
        return Result.ok(vehicleTypeMapper.selectList(null));
    }

    @PostMapping("/vehicle-types")
    public Result<?> addVehicleType(@RequestBody VehicleType vt) {
        vt.setEnabled(1);
        vehicleTypeMapper.insert(vt);
        return Result.ok();
    }

    @PutMapping("/vehicle-types/{id}")
    public Result<?> updateVehicleType(@PathVariable Long id, @RequestBody VehicleType vt) {
        vt.setId(id);
        vehicleTypeMapper.updateById(vt);
        return Result.ok();
    }

    @DeleteMapping("/vehicle-types/{id}")
    public Result<?> deleteVehicleType(@PathVariable Long id) {
        vehicleTypeMapper.deleteById(id);
        return Result.ok();
    }

    @GetMapping("/subjects")
    public Result<?> listSubjects() {
        return Result.ok(subjectMapper.selectList(null));
    }

    @PostMapping("/subjects")
    public Result<?> addSubject(@RequestBody Subject subject) {
        subjectMapper.insert(subject);
        return Result.ok();
    }

    @PutMapping("/subjects/{id}")
    public Result<?> updateSubject(@PathVariable Long id, @RequestBody Subject subject) {
        subject.setId(id);
        subjectMapper.updateById(subject);
        return Result.ok();
    }

    @DeleteMapping("/subjects/{id}")
    public Result<?> deleteSubject(@PathVariable Long id) {
        subjectMapper.deleteById(id);
        return Result.ok();
    }

    @GetMapping("/exam-locations")
    public Result<?> listExamLocations() {
        return Result.ok(examLocationMapper.selectList(null));
    }

    @PostMapping("/exam-locations")
    public Result<?> addExamLocation(@RequestBody ExamLocation location) {
        examLocationMapper.insert(location);
        return Result.ok();
    }

    @PutMapping("/exam-locations/{id}")
    public Result<?> updateExamLocation(@PathVariable Long id, @RequestBody ExamLocation location) {
        location.setId(id);
        examLocationMapper.updateById(location);
        return Result.ok();
    }

    @DeleteMapping("/exam-locations/{id}")
    public Result<?> deleteExamLocation(@PathVariable Long id) {
        examLocationMapper.deleteById(id);
        return Result.ok();
    }

    @GetMapping("/fee-standards")
    public Result<?> listFeeStandards() {
        return Result.ok(feeStandardMapper.selectList(null));
    }

    @PostMapping("/fee-standards")
    public Result<?> addFeeStandard(@RequestBody FeeStandard fs) {
        feeStandardMapper.insert(fs);
        return Result.ok();
    }

    @PutMapping("/fee-standards/{id}")
    public Result<?> updateFeeStandard(@PathVariable Long id, @RequestBody FeeStandard fs) {
        fs.setId(id);
        feeStandardMapper.updateById(fs);
        return Result.ok();
    }

    @DeleteMapping("/fee-standards/{id}")
    public Result<?> deleteFeeStandard(@PathVariable Long id) {
        feeStandardMapper.deleteById(id);
        return Result.ok();
    }

    @GetMapping("/exams")
    public Result<?> listExams() {
        return Result.ok(examRegistrationService.list());
    }

    @PutMapping("/exams/audit/{id}")
    public Result<?> auditExam(@PathVariable Long id, @RequestBody Map<String, String> data) {
        examRegistrationService.adminAuditExam(id, data.get("status"));
        return Result.ok();
    }

    @PutMapping("/exams/score/{id}")
    public Result<?> recordScore(@PathVariable Long id, @RequestBody Map<String, Object> data) {
        java.math.BigDecimal score = new java.math.BigDecimal(data.get("score").toString());
        Integer isPassed = (Integer) data.get("isPassed");
        examRegistrationService.recordScore(id, score, isPassed);
        return Result.ok();
    }

    @GetMapping("/waiting-cert")
    public Result<?> waitingCertStudents() {
        return Result.ok(examRegistrationService.getWaitingCertStudents());
    }

    @PutMapping("/issue-cert/{id}")
    public Result<?> issueCert(@PathVariable Long id) {
        examRegistrationService.issueCert(id);
        return Result.ok();
    }

    @GetMapping("/statistics/registration")
    public Result<?> registrationStats(@RequestParam int year, @RequestParam(required = false) Integer month) {
        return Result.ok(statisticsService.getRegistrationStats(year, month));
    }

    @GetMapping("/statistics/pass-rate")
    public Result<?> passRateStats() {
        return Result.ok(statisticsService.getPassRateStats());
    }

    @GetMapping("/statistics/coach-workload")
    public Result<?> coachWorkloadStats() {
        return Result.ok(statisticsService.getCoachWorkloadStats());
    }

    @GetMapping("/statistics")
    public Result<?> allStats(@RequestParam int year) {
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("registration", statisticsService.getRegistrationStats(year, null));
        result.put("passRate", statisticsService.getPassRateStats());
        result.put("coachWorkload", statisticsService.getCoachWorkloadStats());
        return Result.ok(result);
    }
}
