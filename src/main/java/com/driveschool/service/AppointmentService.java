package com.driveschool.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.driveschool.entity.Appointment;

import java.util.List;

public interface AppointmentService extends IService<Appointment> {
    Appointment createAppointment(Appointment appointment);
    Appointment confirmAppointment(Long appointmentId, Long coachUserId);
    Appointment cancelAppointment(Long appointmentId, Long studentUserId, String reason);
    List<Appointment> getCoachAppointments(Long coachUserId);
    List<Appointment> getStudentAppointments(Long studentUserId);
}
