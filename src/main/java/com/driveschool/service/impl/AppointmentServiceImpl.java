package com.driveschool.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.driveschool.entity.*;
import com.driveschool.mapper.AppointmentMapper;
import com.driveschool.mapper.CoachMapper;
import com.driveschool.mapper.StudentInfoMapper;
import com.driveschool.mapper.UserMapper;
import com.driveschool.service.AppointmentService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class AppointmentServiceImpl extends ServiceImpl<AppointmentMapper, Appointment> implements AppointmentService {

    private final StudentInfoMapper studentInfoMapper;
    private final CoachMapper coachMapper;
    private final UserMapper userMapper;

    public AppointmentServiceImpl(StudentInfoMapper studentInfoMapper, CoachMapper coachMapper,
                                   UserMapper userMapper) {
        this.studentInfoMapper = studentInfoMapper;
        this.coachMapper = coachMapper;
        this.userMapper = userMapper;
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
    public List<Map<String, Object>> getCoachAppointments(Long coachUserId) {
        Coach coach = coachMapper.selectOne(new LambdaQueryWrapper<Coach>().eq(Coach::getUserId, coachUserId));
        if (coach == null) throw new RuntimeException("教练信息不存在");
        List<Appointment> list = list(new LambdaQueryWrapper<Appointment>()
                .eq(Appointment::getCoachId, coach.getId())
                .orderByDesc(Appointment::getCreatedTime));
        List<Map<String, Object>> result = new ArrayList<>();
        for (Appointment a : list) {
            Map<String, Object> map = toMap(a);
            // 关联学员姓名
            StudentInfo si = studentInfoMapper.selectById(a.getStudentId());
            if (si != null) {
                User u = userMapper.selectById(si.getUserId());
                map.put("studentName", u != null ? u.getRealName() : "");
                map.put("studentPhone", u != null ? u.getPhone() : "");
            }
            result.add(map);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getStudentAppointments(Long studentUserId) {
        StudentInfo info = studentInfoMapper.selectOne(
                new LambdaQueryWrapper<StudentInfo>().eq(StudentInfo::getUserId, studentUserId));
        if (info == null) throw new RuntimeException("学员信息不存在");
        List<Appointment> list = list(new LambdaQueryWrapper<Appointment>()
                .eq(Appointment::getStudentId, info.getId())
                .orderByDesc(Appointment::getCreatedTime));
        List<Map<String, Object>> result = new ArrayList<>();
        for (Appointment a : list) {
            Map<String, Object> map = toMap(a);
            // 关联教练姓名
            Coach c = coachMapper.selectById(a.getCoachId());
            if (c != null) {
                User u = userMapper.selectById(c.getUserId());
                map.put("coachName", u != null ? u.getRealName() : "");
            }
            result.add(map);
        }
        return result;
    }

    private Map<String, Object> toMap(Appointment a) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", a.getId());
        map.put("studentId", a.getStudentId());
        map.put("coachId", a.getCoachId());
        map.put("appointmentTime", a.getAppointmentTime());
        map.put("appointmentDate", a.getAppointmentDate());
        map.put("status", a.getStatus());
        map.put("cancelReason", a.getCancelReason());
        map.put("createdTime", a.getCreatedTime());
        return map;
    }
}
