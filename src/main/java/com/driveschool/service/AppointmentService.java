package com.driveschool.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.driveschool.entity.Appointment;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface AppointmentService extends IService<Appointment> {
    Appointment createAppointment(Appointment appointment);
    Appointment confirmAppointment(Long appointmentId, Long coachUserId);
    Appointment cancelAppointment(Long appointmentId, Long studentUserId, String reason);
    List<Map<String, Object>> getCoachAppointments(Long coachUserId);
    List<Map<String, Object>> getStudentAppointments(Long studentUserId);

    /**
     * 查询指定教练在指定日期的可用时间槽，按上午/下午分组
     * @param coachId 教练ID
     * @param date   查询日期
     * @return { date, dayOfWeek, morning: { available, slots[] }, afternoon: { available, slots[] } }
     */
    Map<String, Object> getCoachAvailableSlots(Long coachId, LocalDate date);

    /**
     * 查询指定教练在日期范围内哪些日期有至少一个可用时间槽
     * @param coachId   教练ID
     * @param startDate 开始日期（含）
     * @param endDate   结束日期（含）
     * @return 有可用槽位的日期列表 ["2024-06-17", "2024-06-18", ...]
     */
    List<String> getAvailableDates(Long coachId, LocalDate startDate, LocalDate endDate);
}
