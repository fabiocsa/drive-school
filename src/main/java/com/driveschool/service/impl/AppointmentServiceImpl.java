package com.driveschool.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.driveschool.entity.*;
import com.driveschool.mapper.AppointmentMapper;
import com.driveschool.mapper.CoachMapper;
import com.driveschool.mapper.StudentInfoMapper;
import com.driveschool.service.AppointmentService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class AppointmentServiceImpl extends ServiceImpl<AppointmentMapper, Appointment> implements AppointmentService {

    private final StudentInfoMapper studentInfoMapper;
    private final CoachMapper coachMapper;

    public AppointmentServiceImpl(StudentInfoMapper studentInfoMapper, CoachMapper coachMapper) {
        this.studentInfoMapper = studentInfoMapper;
        this.coachMapper = coachMapper;
    }

    @Override
    public Appointment createAppointment(Appointment appointment) {
        StudentInfo info = studentInfoMapper.selectById(appointment.getStudentId());
        if (info == null || info.getCoachId() == null) {
            throw new RuntimeException("您尚未分配教练");
        }
        appointment.setStatus("PENDING");
        save(appointment);
        return appointment;
    }

    @Override
    public Appointment confirmAppointment(Long appointmentId, Long coachUserId) {
        Appointment appointment = getById(appointmentId);
        if (appointment == null) throw new RuntimeException("约课记录不存在");
        Coach coach = coachMapper.selectOne(new LambdaQueryWrapper<Coach>().eq(Coach::getUserId, coachUserId));
        if (coach == null || !coach.getId().equals(appointment.getCoachId())) {
            throw new RuntimeException("无权操作此约课");
        }
        appointment.setStatus("CONFIRMED");
        updateById(appointment);
        return appointment;
    }

    @Override
    public Appointment cancelAppointment(Long appointmentId, Long studentUserId, String reason) {
        Appointment appointment = getById(appointmentId);
        if (appointment == null) throw new RuntimeException("约课记录不存在");
        StudentInfo info = studentInfoMapper.selectOne(
                new LambdaQueryWrapper<StudentInfo>().eq(StudentInfo::getUserId, studentUserId));
        if (info == null || !info.getId().equals(appointment.getStudentId())) {
            throw new RuntimeException("无权操作此约课");
        }
        if (appointment.getAppointmentDate().isBefore(LocalDate.now().plusDays(1))) {
            throw new RuntimeException("只能提前24小时取消约课");
        }
        appointment.setStatus("CANCELLED");
        appointment.setCancelReason(reason);
        updateById(appointment);
        return appointment;
    }

    @Override
    public List<Appointment> getCoachAppointments(Long coachUserId) {
        Coach coach = coachMapper.selectOne(new LambdaQueryWrapper<Coach>().eq(Coach::getUserId, coachUserId));
        if (coach == null) throw new RuntimeException("教练信息不存在");
        return list(new LambdaQueryWrapper<Appointment>()
                .eq(Appointment::getCoachId, coach.getId())
                .orderByDesc(Appointment::getCreatedTime));
    }

    @Override
    public List<Appointment> getStudentAppointments(Long studentUserId) {
        StudentInfo info = studentInfoMapper.selectOne(
                new LambdaQueryWrapper<StudentInfo>().eq(StudentInfo::getUserId, studentUserId));
        if (info == null) throw new RuntimeException("学员信息不存在");
        return list(new LambdaQueryWrapper<Appointment>()
                .eq(Appointment::getStudentId, info.getId())
                .orderByDesc(Appointment::getCreatedTime));
    }
}
