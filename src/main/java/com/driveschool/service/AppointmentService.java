package com.driveschool.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.driveschool.entity.Appointment;

import java.util.List;
import java.util.Map;

public interface AppointmentService extends IService<Appointment> {
    Appointment createAppointment(Appointment appointment);
    Appointment confirmAppointment(Long appointmentId, Long coachUserId);
    Appointment cancelAppointment(Long appointmentId, Long studentUserId, String reason);
    List<Map<String, Object>> getCoachAppointments(Long coachUserId);
    List<Map<String, Object>> getStudentAppointments(Long studentUserId);
}
