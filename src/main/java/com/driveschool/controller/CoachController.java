package com.driveschool.controller;

import com.driveschool.entity.*;
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

    @GetMapping("/myinfo")
    public Result<?> getMyInfo(@RequestParam Long userId) {
        Map<String, Object> info = coachService.getMyCoachInfo(userId);
        return Result.ok(info);
    }

    @PostMapping("/training/record")
    public Result<?> recordTraining(@RequestBody TrainingHour hour) {
        return Result.ok(trainingHourService.record(hour));
    }

    @GetMapping("/students/trainings")
    public Result<?> getStudentTrainings(@RequestParam Long studentId) {
        return Result.ok(trainingHourService.getStudentRecords(studentId));
    }

    @GetMapping("/appointments")
    public Result<?> getAppointments(@RequestParam Long userId) {
        return Result.ok(appointmentService.getCoachAppointments(userId));
    }

    @PutMapping("/appointment/confirm/{id}")
    public Result<?> confirmAppointment(@PathVariable Long id, @RequestParam Long userId) {
        return Result.ok(appointmentService.confirmAppointment(id, userId));
    }
}
