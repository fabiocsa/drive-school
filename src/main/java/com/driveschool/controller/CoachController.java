package com.driveschool.controller;

import com.driveschool.entity.*;
import com.driveschool.security.SecurityUtils;
import com.driveschool.service.*;
import com.driveschool.util.Result;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/coach")
public class CoachController {

    private final TrainingHourService trainingHourService;
    private final AppointmentService appointmentService;
    private final CoachService coachService;

    public CoachController(TrainingHourService trainingHourService,
                            AppointmentService appointmentService,
                            CoachService coachService) {
        this.trainingHourService = trainingHourService;
        this.appointmentService = appointmentService;
        this.coachService = coachService;
    }

    /**
     * 获取当前登录教练的个人信息（userId 从 JWT 中获取）
     */
    @GetMapping("/myinfo")
    public Result<?> getMyInfo() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return Result.fail("未登录或登录已过期");
        }
        Map<String, Object> info = coachService.getMyCoachInfo(userId);
        return Result.ok(info);
    }

    /**
     * 记录学员学时（验证学员属于当前教练）
     */
    @PostMapping("/training/record")
    public Result<?> recordTraining(@RequestBody TrainingHour hour) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return Result.fail("未登录或登录已过期");
        }
        Coach coach = coachService.getCoachByUserId(userId);
        if (coach == null) {
            return Result.fail("教练信息不存在");
        }
        // 验证学员属于当前教练
        if (!coachService.isStudentAssignedToCoach(hour.getStudentId(), coach.getId())) {
            return Result.fail("无权为该学员记录学时");
        }
        return Result.ok(trainingHourService.record(hour));
    }

    /**
     * 查询学员学时记录（验证学员属于当前教练）
     */
    @GetMapping("/students/trainings")
    public Result<?> getStudentTrainings(@RequestParam Long studentId) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return Result.fail("未登录或登录已过期");
        }
        Coach coach = coachService.getCoachByUserId(userId);
        if (coach == null) {
            return Result.fail("教练信息不存在");
        }
        if (!coachService.isStudentAssignedToCoach(studentId, coach.getId())) {
            return Result.fail("无权查看该学员的学时记录");
        }
        return Result.ok(trainingHourService.getStudentRecords(studentId));
    }

    /**
     * 获取当前教练的约课列表
     */
    @GetMapping("/appointments")
    public Result<?> getAppointments() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return Result.fail("未登录或登录已过期");
        }
        return Result.ok(appointmentService.getCoachAppointments(userId));
    }

    /**
     * 确认约课（验证预约属于当前教练）
     */
    @PutMapping("/appointment/confirm/{id}")
    public Result<?> confirmAppointment(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return Result.fail("未登录或登录已过期");
        }
        return Result.ok(appointmentService.confirmAppointment(id, userId));
    }

    /**
     * 调整学员学习阶段（验证学员属于当前教练）
     */
    @PutMapping("/phase/{studentId}")
    public Result<?> adjustPhase(@PathVariable Long studentId, @RequestBody Map<String, String> data) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return Result.fail("未登录或登录已过期");
        }
        Coach coach = coachService.getCoachByUserId(userId);
        if (coach == null) {
            return Result.fail("教练信息不存在");
        }
        if (!coachService.isStudentAssignedToCoach(studentId, coach.getId())) {
            return Result.fail("无权调整该学员的学习阶段");
        }
        String newPhase = data.get("newPhase");
        trainingHourService.manualAdjustPhase(studentId, newPhase);
        return Result.ok();
    }
}
