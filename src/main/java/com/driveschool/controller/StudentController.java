package com.driveschool.controller;

import com.driveschool.entity.*;
import com.driveschool.service.*;
import com.driveschool.util.Result;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @GetMapping("/info")
    public Result<?> getMyInfo(@RequestAttribute("username") String username) {
        return null;
    }

    @PostMapping("/register")
    public Result<?> submitRegistration(@RequestBody Map<String, Object> data) {
        StudentInfo info = studentInfoService.submitRegistration(data);
        return Result.ok(info);
    }

    @PostMapping("/upload")
    public Result<?> uploadFile(@RequestParam("file") MultipartFile file) {
        String filename = studentInfoService.uploadFile(file);
        return Result.ok(Map.of("filename", filename));
    }

    @GetMapping("/myinfo")
    public Result<?> getMyInfo(@RequestParam Long userId) {
        Map<String, Object> info = studentInfoService.getMyInfo(userId);
        return Result.ok(info);
    }

    @GetMapping("/trainings")
    public Result<?> getTrainings(@RequestParam Long studentId) {
        return Result.ok(trainingHourService.getStudentRecords(studentId));
    }

    @PostMapping("/appointment")
    public Result<?> createAppointment(@RequestBody Appointment appointment) {
        return Result.ok(appointmentService.createAppointment(appointment));
    }

    @PutMapping("/appointment/cancel/{id}")
    public Result<?> cancelAppointment(@PathVariable Long id, @RequestBody Map<String, String> data) {
        Long userId = Long.valueOf(data.get("userId"));
        String reason = data.get("reason");
        return Result.ok(appointmentService.cancelAppointment(id, userId, reason));
    }

    @GetMapping("/appointments")
    public Result<?> getMyAppointments(@RequestParam Long userId) {
        return Result.ok(appointmentService.getStudentAppointments(userId));
    }

    @PostMapping("/exam/register")
    public Result<?> registerExam(@RequestBody Map<String, Object> data) {
        Long userId = Long.valueOf(data.get("userId").toString());
        return Result.ok(examRegistrationService.registerExam(userId, data));
    }

    @GetMapping("/exams")
    public Result<?> getMyExams(@RequestParam Long userId) {
        return Result.ok(examRegistrationService.getStudentExams(userId));
    }

    @GetMapping("/pdf/{studentId}")
    public Result<?> getPdfList(@PathVariable Long studentId) {
        return Result.ok(Map.of(
            "registration", studentInfoService.downloadPdf(studentId, "registration") != null,
            "health", studentInfoService.downloadPdf(studentId, "health") != null,
            "examcard", studentInfoService.downloadPdf(studentId, "examcard") != null
        ));
    }

    @GetMapping("/pdf/download/{studentId}/{pdfType}")
    public byte[] downloadPdf(@PathVariable Long studentId, @PathVariable String pdfType,
                               javax.servlet.http.HttpServletResponse response) {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=" + pdfType + ".pdf");
        return studentInfoService.downloadPdf(studentId, pdfType);
    }
}
