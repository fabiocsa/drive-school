package com.driveschool.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.driveschool.entity.Coach;
import com.driveschool.entity.LearningPhase;
import com.driveschool.entity.StudentInfo;
import com.driveschool.entity.TrainingHour;
import com.driveschool.entity.User;
import com.driveschool.mapper.*;
import com.driveschool.service.CoachService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CoachServiceImpl extends ServiceImpl<CoachMapper, Coach> implements CoachService {

    private final UserMapper userMapper;
    private final StudentInfoMapper studentInfoMapper;
    private final LearningPhaseMapper learningPhaseMapper;
    private final TrainingHourMapper trainingHourMapper;

    public CoachServiceImpl(UserMapper userMapper, StudentInfoMapper studentInfoMapper,
                            LearningPhaseMapper learningPhaseMapper,
                            TrainingHourMapper trainingHourMapper) {
        this.userMapper = userMapper;
        this.studentInfoMapper = studentInfoMapper;
        this.learningPhaseMapper = learningPhaseMapper;
        this.trainingHourMapper = trainingHourMapper;
    }

    /**
     * 默认每时段容量——当 schedule_json 使用旧格式（纯字符串数组）时，所有时段
     * 自动继承此默认值；新格式中每项可单独指定 capacity。
     */
    public static final int DEFAULT_SLOT_CAPACITY = 5;

    @Override
    public List<Map<String, Object>> recommendCoaches(Long vehicleTypeId) {
        List<Coach> coaches = list(new LambdaQueryWrapper<Coach>()
                .eq(Coach::getVehicleTypeId, vehicleTypeId)
                .isNotNull(Coach::getScheduleJson)
                .ne(Coach::getScheduleJson, "[]")
                .orderByDesc(Coach::getRating));
        List<Map<String, Object>> result = new ArrayList<>();
        int count = 0;
        for (Coach coach : coaches) {
            if (count >= 5) break; // 扩展为最多推荐 5 名教练
            Map<String, Object> map = new HashMap<>();
            User user = userMapper.selectById(coach.getUserId());
            map.put("coach", coach);
            map.put("name", user != null ? user.getRealName() : "");
            map.put("phone", user != null ? user.getPhone() : "");

            // 工作量数据：当前带教学员数 + 总授课学时
            Long studentCount = studentInfoMapper.countByCoachId(coach.getId());
            java.math.BigDecimal totalHours = trainingHourMapper.sumDurationByCoachId(coach.getId());
            map.put("studentCount", studentCount != null ? studentCount : 0);
            map.put("totalHours", totalHours != null ? totalHours : java.math.BigDecimal.ZERO);

            // 综合评分：rating(1-5) → 60%权重 + 空闲指数(按0学员得满分)占40%
            // 简单公式：score = rating * 12 + Math.max(0, 40 - studentCount * 2)
            int compositeScore = coach.getRating() != null ? coach.getRating() * 12 : 36;
            compositeScore += Math.max(0, 40 - (studentCount != null ? studentCount.intValue() : 0) * 2);
            map.put("compositeScore", compositeScore);

            result.add(map);
            count++;
        }
        // 按复合评分降序排列
        result.sort((a, b) -> Integer.compare(
                (Integer) b.get("compositeScore"), (Integer) a.get("compositeScore")));
        return result;
    }

    @Override
    public List<Map<String, Object>> listWithDetails() {
        List<Coach> coaches = list();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Coach coach : coaches) {
            Map<String, Object> map = new HashMap<>();
            User user = userMapper.selectById(coach.getUserId());
            map.put("id", coach.getId());
            map.put("userId", coach.getUserId());
            map.put("gender", coach.getGender());
            map.put("vehicleTypeId", coach.getVehicleTypeId());
            map.put("rating", coach.getRating());
            map.put("scheduleJson", coach.getScheduleJson());
            map.put("realName", user != null ? user.getRealName() : "");
            map.put("phone", user != null ? user.getPhone() : "");
            result.add(map);
        }
        return result;
    }

    @Override
    public Map<String, Object> getMyCoachInfo(Long userId) {
        Coach coach = getOne(new LambdaQueryWrapper<Coach>().eq(Coach::getUserId, userId));
        if (coach == null) return null;
        User user = userMapper.selectById(userId);
        Map<String, Object> map = new HashMap<>();
        map.put("coach", coach);
        map.put("realName", user != null ? user.getRealName() : "");
        map.put("phone", user != null ? user.getPhone() : "");
        return map;
    }

    @Override
    public Coach getCoachByUserId(Long userId) {
        return getOne(new LambdaQueryWrapper<Coach>().eq(Coach::getUserId, userId));
    }

    @Override
    public boolean isStudentAssignedToCoach(Long studentInfoId, Long coachId) {
        StudentInfo info = studentInfoMapper.selectById(studentInfoId);
        return info != null && info.getCoachId() != null && info.getCoachId().equals(coachId);
    }

    // ============================================================
    // 新增：更新教练空闲档期（含容量）
    // ============================================================
    @Override
    public Coach updateSchedule(Long userId, String scheduleJson) {
        Coach coach = getOne(new LambdaQueryWrapper<Coach>().eq(Coach::getUserId, userId));
        if (coach == null) {
            throw new RuntimeException("教练信息不存在");
        }
        // 基本格式校验（合法的 JSON 数组）
        if (scheduleJson == null || scheduleJson.isBlank()) {
            throw new RuntimeException("档期不能为空");
        }
        String trimmed = scheduleJson.trim();
        if (!trimmed.startsWith("[") || !trimmed.endsWith("]")) {
            throw new RuntimeException("档期格式不合法（需为 JSON 数组）");
        }
        // 长度约束
        if (trimmed.length() > 1000) {
            throw new RuntimeException("档期数据过长（上限1000字符）");
        }
        coach.setScheduleJson(trimmed);
        updateById(coach);
        return coach;
    }

    // ============================================================
    // 新增：模糊搜索教练名下的学员
    // ============================================================
    @Override
    public List<Map<String, Object>> searchStudents(Long coachUserId, String keyword) {
        Coach coach = getOne(new LambdaQueryWrapper<Coach>().eq(Coach::getUserId, coachUserId));
        if (coach == null) return Collections.emptyList();

        // 获取教练名下所有学员
        List<StudentInfo> students = studentInfoMapper.selectList(
                new LambdaQueryWrapper<StudentInfo>().eq(StudentInfo::getCoachId, coach.getId()));

        List<Map<String, Object>> result = new ArrayList<>();
        for (StudentInfo si : students) {
            User u = userMapper.selectById(si.getUserId());
            String realName = u != null ? (u.getRealName() != null ? u.getRealName() : "") : "";
            String phone = u != null ? (u.getPhone() != null ? u.getPhone() : "") : "";

            // 模糊匹配：姓名、学号(ID)、手机号
            if (keyword != null && !keyword.isBlank()) {
                String kw = keyword.toLowerCase().trim();
                boolean match = realName.contains(kw)
                        || String.valueOf(si.getId()).contains(kw)
                        || phone.contains(kw);
                if (!match) continue;
            }

            Map<String, Object> map = new HashMap<>();
            map.put("studentInfo", si);
            map.put("realName", realName);
            map.put("phone", phone);
            LearningPhase phase = learningPhaseMapper.selectOne(
                    new LambdaQueryWrapper<LearningPhase>()
                            .eq(LearningPhase::getStudentId, si.getId()));
            map.put("learningPhase", phase);
            result.add(map);
        }
        return result;
    }

    // ============================================================
    // 新增：获取学员详细信息摘要
    // ============================================================
    @Override
    public Map<String, Object> getStudentSummary(Long coachUserId, Long studentInfoId) {
        Coach coach = getOne(new LambdaQueryWrapper<Coach>().eq(Coach::getUserId, coachUserId));
        if (coach == null) throw new RuntimeException("教练信息不存在");

        StudentInfo si = studentInfoMapper.selectById(studentInfoId);
        if (si == null || !si.getCoachId().equals(coach.getId())) {
            throw new RuntimeException("无权查看该学员信息");
        }

        User u = userMapper.selectById(si.getUserId());
        LearningPhase phase = learningPhaseMapper.selectOne(
                new LambdaQueryWrapper<LearningPhase>().eq(LearningPhase::getStudentId, studentInfoId));

        // 汇总累计学时
        List<TrainingHour> hours = trainingHourMapper.selectList(
                new LambdaQueryWrapper<TrainingHour>().eq(TrainingHour::getStudentId, studentInfoId));
        BigDecimal totalHours = hours.stream()
                .map(TrainingHour::getDuration)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("studentInfoId", si.getId());
        result.put("realName", u != null ? u.getRealName() : "");
        result.put("phone", u != null ? u.getPhone() : "");
        result.put("idCard", si.getIdCard());
        result.put("address", si.getAddress());
        result.put("auditStatus", si.getAuditStatus());
        result.put("assignStatus", si.getAssignStatus());
        result.put("currentPhase", phase != null ? phase.getCurrentPhase() : "PHASE1");
        result.put("totalHours", totalHours);
        result.put("phase1Hours", phase != null ? phase.getPhase1Hours() : BigDecimal.ZERO);
        result.put("phase2Hours", phase != null ? phase.getPhase2Hours() : BigDecimal.ZERO);
        result.put("phase3Hours", phase != null ? phase.getPhase3Hours() : BigDecimal.ZERO);
        result.put("phase4Hours", phase != null ? phase.getPhase4Hours() : BigDecimal.ZERO);
        // 剩余学时：根据车型要求的最低学时减去已累计学时（简化处理，展示总学时即可）
        result.put("trainingCount", hours.size());

        return result;
    }
}
