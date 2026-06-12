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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AppointmentServiceImpl extends ServiceImpl<AppointmentMapper, Appointment> implements AppointmentService {

    private final StudentInfoMapper studentInfoMapper;
    private final CoachMapper coachMapper;
    private final UserMapper userMapper;

    // ============================================================
    // 时间槽定义：每个半日拆分为若干 2 小时的时段
    // 上午 08:00-12:00 → 2 个槽，下午 13:00-17:00 → 2 个槽
    // 可根据实际业务调整
    // ============================================================
    private static final List<String> MORNING_SLOTS = List.of("08:00-10:00", "10:00-12:00");
    private static final List<String> AFTERNOON_SLOTS = List.of("13:00-15:00", "15:00-17:00");

    /** 中文周几映射：DayOfWeek → "周一"..."周日" */
    private static final Map<DayOfWeek, String> DAY_OF_WEEK_CN = Map.of(
            DayOfWeek.MONDAY, "周一",
            DayOfWeek.TUESDAY, "周二",
            DayOfWeek.WEDNESDAY, "周三",
            DayOfWeek.THURSDAY, "周四",
            DayOfWeek.FRIDAY, "周五",
            DayOfWeek.SATURDAY, "周六",
            DayOfWeek.SUNDAY, "周日"
    );

    public AppointmentServiceImpl(StudentInfoMapper studentInfoMapper, CoachMapper coachMapper,
                                   UserMapper userMapper) {
        this.studentInfoMapper = studentInfoMapper;
        this.coachMapper = coachMapper;
        this.userMapper = userMapper;
    }

    // ============================================================
    // 核心：创建约课（增加冲突校验）
    // ============================================================
    @Override
    public Appointment createAppointment(Appointment appointment) {
        // 1. 验证学员信息及教练归属
        StudentInfo info = studentInfoMapper.selectById(appointment.getStudentId());
        if (info == null || info.getCoachId() == null) {
            throw new RuntimeException("您尚未分配教练");
        }
        // 确保 appointment 中的 coachId 来自学员的绑定教练（防止前端篡改）
        appointment.setCoachId(info.getCoachId());

        // 2. 校验日期不能是过去
        if (appointment.getAppointmentDate() == null || appointment.getAppointmentDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("约课日期不能是过去");
        }

        // 3. 校验所选时间槽是否在教练的排班范围内
        Coach coach = coachMapper.selectById(appointment.getCoachId());
        if (coach == null) {
            throw new RuntimeException("教练信息不存在");
        }
        String scheduleJson = coach.getScheduleJson();
        if (scheduleJson == null || scheduleJson.equals("[]")) {
            throw new RuntimeException("该教练暂无排班");
        }

        // 解析教练每周排班
        Set<String> weeklySchedule = parseScheduleJson(scheduleJson);
        String dayOfWeekCn = getDayOfWeekCn(appointment.getAppointmentDate());
        String halfDay = getHalfDay(appointment.getAppointmentTime());

        // 教练在该半日是否上班
        String requiredToken = dayOfWeekCn + halfDay; // 如 "周一上午"
        if (!weeklySchedule.contains(requiredToken)) {
            throw new RuntimeException("该时段不在教练排班范围内");
        }

        // 4. 冲突校验：同一教练+日期+时间槽不能有非取消的预约
        long conflictCount = count(new LambdaQueryWrapper<Appointment>()
                .eq(Appointment::getCoachId, appointment.getCoachId())
                .eq(Appointment::getAppointmentDate, appointment.getAppointmentDate())
                .eq(Appointment::getAppointmentTime, appointment.getAppointmentTime())
                .notIn(Appointment::getStatus, "CANCELLED"));
        if (conflictCount > 0) {
            throw new RuntimeException("该时间段已被预约，请选择其他时间");
        }

        // 5. 入库
        appointment.setStatus("PENDING");
        save(appointment);
        return appointment;
    }

    // ============================================================
    // 新增：查询教练在指定日期的可用时间槽（上午/下午分组）
    // ============================================================
    @Override
    public Map<String, Object> getCoachAvailableSlots(Long coachId, LocalDate date) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("date", date.format(DateTimeFormatter.ISO_LOCAL_DATE));
        String dayOfWeekCn = getDayOfWeekCn(date);
        result.put("dayOfWeek", dayOfWeekCn);

        // 读取教练排班
        Coach coach = coachMapper.selectById(coachId);
        Set<String> weeklySchedule;
        if (coach == null || coach.getScheduleJson() == null || coach.getScheduleJson().equals("[]")) {
            weeklySchedule = Collections.emptySet();
        } else {
            weeklySchedule = parseScheduleJson(coach.getScheduleJson());
        }

        // 查询该教练该日期已被约的时间槽（排除已取消）
        List<Appointment> booked = list(new LambdaQueryWrapper<Appointment>()
                .eq(Appointment::getCoachId, coachId)
                .eq(Appointment::getAppointmentDate, date)
                .notIn(Appointment::getStatus, "CANCELLED"));
        Set<String> bookedSlots = booked.stream()
                .map(Appointment::getAppointmentTime)
                .collect(Collectors.toSet());

        // 上午
        boolean morningAvailable = weeklySchedule.contains(dayOfWeekCn + "上午");
        List<String> morningSlots = morningAvailable
                ? MORNING_SLOTS.stream().filter(s -> !bookedSlots.contains(s)).collect(Collectors.toList())
                : Collections.emptyList();

        Map<String, Object> morning = new LinkedHashMap<>();
        morning.put("available", !morningSlots.isEmpty());
        morning.put("slots", morningSlots);
        result.put("morning", morning);

        // 下午
        boolean afternoonAvailable = weeklySchedule.contains(dayOfWeekCn + "下午");
        List<String> afternoonSlots = afternoonAvailable
                ? AFTERNOON_SLOTS.stream().filter(s -> !bookedSlots.contains(s)).collect(Collectors.toList())
                : Collections.emptyList();

        Map<String, Object> afternoon = new LinkedHashMap<>();
        afternoon.put("available", !afternoonSlots.isEmpty());
        afternoon.put("slots", afternoonSlots);
        result.put("afternoon", afternoon);

        return result;
    }

    // ============================================================
    // 新增：查询教练在日期范围内有可用槽位的日期列表
    // ============================================================
    @Override
    public List<String> getAvailableDates(Long coachId, LocalDate startDate, LocalDate endDate) {
        // 读取教练排班
        Coach coach = coachMapper.selectById(coachId);
        Set<String> weeklySchedule;
        if (coach == null || coach.getScheduleJson() == null || coach.getScheduleJson().equals("[]")) {
            return Collections.emptyList();
        }
        weeklySchedule = parseScheduleJson(coach.getScheduleJson());
        if (weeklySchedule.isEmpty()) {
            return Collections.emptyList();
        }

        // 查询该教练在日期范围内所有已约记录（排除已取消），按日期分组统计已约槽位
        List<Appointment> booked = list(new LambdaQueryWrapper<Appointment>()
                .eq(Appointment::getCoachId, coachId)
                .between(Appointment::getAppointmentDate, startDate, endDate)
                .notIn(Appointment::getStatus, "CANCELLED"));

        // bookedSlotsByDate: date -> Set<timeSlot>
        Map<LocalDate, Set<String>> bookedSlotsByDate = new HashMap<>();
        for (Appointment a : booked) {
            bookedSlotsByDate.computeIfAbsent(a.getAppointmentDate(), k -> new HashSet<>())
                    .add(a.getAppointmentTime());
        }

        List<String> availableDates = new ArrayList<>();
        LocalDate d = startDate;
        while (!d.isAfter(endDate)) {
            String dayOfWeekCn = getDayOfWeekCn(d);
            int availableCount = 0;

            // 上午
            if (weeklySchedule.contains(dayOfWeekCn + "上午")) {
                Set<String> bookedToday = bookedSlotsByDate.getOrDefault(d, Collections.emptySet());
                availableCount += MORNING_SLOTS.stream().filter(s -> !bookedToday.contains(s)).count();
            }
            // 下午
            if (weeklySchedule.contains(dayOfWeekCn + "下午")) {
                Set<String> bookedToday = bookedSlotsByDate.getOrDefault(d, Collections.emptySet());
                availableCount += AFTERNOON_SLOTS.stream().filter(s -> !bookedToday.contains(s)).count();
            }

            if (availableCount > 0) {
                availableDates.add(d.format(DateTimeFormatter.ISO_LOCAL_DATE));
            }
            d = d.plusDays(1);
        }
        return availableDates;
    }

    // ============================================================
    // 以下为原有方法（保持不变）
    // ============================================================

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
            Coach c = coachMapper.selectById(a.getCoachId());
            if (c != null) {
                User u = userMapper.selectById(c.getUserId());
                map.put("coachName", u != null ? u.getRealName() : "");
            }
            result.add(map);
        }
        return result;
    }

    // ============================================================
    // 工具方法
    // ============================================================

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

    /** 解析 coach.schedule_json，如 ["周一上午","周一下午",...] → Set */
    private Set<String> parseScheduleJson(String json) {
        // scheduleJson 格式: ["周一上午","周一下午","周二上午",...]
        if (json == null || json.isBlank()) return Collections.emptySet();
        return Arrays.stream(json.replaceAll("[\\[\\]\"]", "").split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    /** 根据日期获取中文周几，如 LocalDate(2024-06-17 周一) → "周一" */
    private String getDayOfWeekCn(LocalDate date) {
        return DAY_OF_WEEK_CN.getOrDefault(date.getDayOfWeek(), "");
    }

    /** 根据时间槽判断属于上午还是下午，用于排班校验 */
    private String getHalfDay(String timeSlot) {
        if (timeSlot == null) return "";
        // 时间槽格式: "08:00-10:00" → 上午, "13:00-15:00" → 下午
        String startHour = timeSlot.split(":")[0];
        int hour = Integer.parseInt(startHour);
        return hour < 12 ? "上午" : "下午";
    }
}
