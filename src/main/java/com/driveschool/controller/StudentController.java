package com.driveschool.controller;

import com.driveschool.entity.*;
import com.driveschool.security.SecurityUtils;
import com.driveschool.service.*;
import com.driveschool.util.Result;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    private final StudentInfoService studentInfoService;
    private final TrainingHourService trainingHourService;
    private final AppointmentService appointmentService;
    private final ExamRegistrationService examRegistrationService;

    public StudentController(StudentInfoService studentInfoService, TrainingHourService trainingHourService,
                              AppointmentService appointmentService, ExamRegistrationService examRegistrationService) {
        this.studentInfoService = studentInfoService;
        this.trainingHourService = trainingHourService;
        this.appointmentService = appointmentService;
        this.examRegistrationService = examRegistrationService;
    }

    /**
     * 获取当前登录学员的个人信息（使用 JWT 中的 userId，防止越权访问）
     */
    @GetMapping("/myinfo")
    public Result<?> getMyInfo() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return Result.fail("未登录或登录已过期");
        }
        Map<String, Object> info = studentInfoService.getMyInfo(userId);
        return Result.ok(info);
    }

    /**
     * 提交报名信息（userId 从 JWT 中获取，不允许客户端指定）
     */
    @PostMapping("/register")
    public Result<?> submitRegistration(@RequestBody Map<String, Object> data) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return Result.fail("未登录或登录已过期");
        }
        data.put("userId", userId);
        StudentInfo info = studentInfoService.submitRegistration(data);
        return Result.ok(info);
    }

    @PostMapping("/upload")
    public Result<?> uploadFile(@RequestParam("file") MultipartFile file) {
        String filename = studentInfoService.uploadFile(file);
        return Result.ok(Map.of("filename", filename));
    }

    /**
     * 获取当前学员的学时记录（通过 userId 查询对应的 studentInfoId，防止越权）
     */
    @GetMapping("/trainings")
    public Result<?> getTrainings() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return Result.fail("未登录或登录已过期");
        }
        // 通过 userId 查找对应的 studentInfoId，确保只能查自己的学时
        Long studentInfoId = studentInfoService.getStudentInfoIdByUserId(userId);
        if (studentInfoId == null) {
            return Result.fail("学员信息不存在，请先完成报名");
        }
        return Result.ok(trainingHourService.getStudentRecords(studentInfoId));
    }

    /**
     * 创建约课（自动注入当前学员 ID，防止伪造其他学员的约课）
     */
    @PostMapping("/appointment")
    public Result<?> createAppointment(@RequestBody Appointment appointment) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return Result.fail("未登录或登录已过期");
        }
        Long studentInfoId = studentInfoService.getStudentInfoIdByUserId(userId);
        if (studentInfoId == null) {
            return Result.fail("学员信息不存在，请先完成报名");
        }
        appointment.setStudentId(studentInfoId);
        return Result.ok(appointmentService.createAppointment(appointment));
    }

    /**
     * 取消约课（验证预约属于当前登录学员）
     */
    @PutMapping("/appointment/cancel/{id}")
    public Result<?> cancelAppointment(@PathVariable Long id, @RequestBody Map<String, String> data) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return Result.fail("未登录或登录已过期");
        }
        String reason = data.get("reason");
        return Result.ok(appointmentService.cancelAppointment(id, userId, reason));
    }

    /**
     * 获取当前登录学员的约课列表
     */
    @GetMapping("/appointments")
    public Result<?> getMyAppointments() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return Result.fail("未登录或登录已过期");
        }
        return Result.ok(appointmentService.getStudentAppointments(userId));
    }

    /**
     * 报名考试（userId 从 JWT 中获取）
     */
    @PostMapping("/exam/register")
    public Result<?> registerExam(@RequestBody Map<String, Object> data) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return Result.fail("未登录或登录已过期");
        }
        return Result.ok(examRegistrationService.registerExam(userId, data));
    }

    /**
     * 获取当前登录学员的考试记录
     */
    @GetMapping("/exams")
    public Result<?> getMyExams() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return Result.fail("未登录或登录已过期");
        }
        return Result.ok(examRegistrationService.getStudentExams(userId));
    }

    /**
     * 获取 PDF 下载列表（验证 studentId 属于当前登录学员）
     */
    @GetMapping("/pdf/{studentId}")
    public Result<?> getPdfList(@PathVariable Long studentId) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return Result.fail("未登录或登录已过期");
        }
        if (!studentInfoService.isStudentOwnedByUser(studentId, userId)) {
            return Result.fail("无权访问该学员的PDF");
        }
        boolean reg = studentInfoService.downloadPdf(studentId, "registration") != null;
        boolean health = studentInfoService.downloadPdf(studentId, "health") != null;
        boolean examcard = studentInfoService.downloadPdf(studentId, "examcard") != null;
        return Result.ok(Map.of("registration", reg, "health", health, "examcard", examcard));
    }

    /**
     * 下载 PDF（验证 studentId 属于当前登录学员）
     */
    @GetMapping("/pdf/download/{studentId}/{pdfType}")
    public void downloadPdf(@PathVariable Long studentId, @PathVariable String pdfType,
                             HttpServletResponse response) throws Exception {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "未登录");
            return;
        }
        if (!studentInfoService.isStudentOwnedByUser(studentId, userId)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "无权下载");
            return;
        }
        byte[] pdfBytes = studentInfoService.downloadPdf(studentId, pdfType);
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=" + pdfType + ".pdf");
        response.getOutputStream().write(pdfBytes);
    }
}
