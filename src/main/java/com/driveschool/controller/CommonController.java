package com.driveschool.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.driveschool.entity.StudentInfo;
import com.driveschool.entity.User;
import com.driveschool.mapper.*;
import com.driveschool.security.SecurityUtils;
import com.driveschool.util.Result;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/common")
public class CommonController {

    private final VehicleTypeMapper vehicleTypeMapper;
    private final SubjectMapper subjectMapper;
    private final ExamLocationMapper examLocationMapper;
    private final StudentInfoMapper studentInfoMapper;
    private final UserMapper userMapper;
    private final CoachMapper coachMapper;
    private final com.driveschool.mapper.LearningPhaseMapper learningPhaseMapper;

    public CommonController(VehicleTypeMapper vehicleTypeMapper, SubjectMapper subjectMapper,
                             ExamLocationMapper examLocationMapper, StudentInfoMapper studentInfoMapper,
                             UserMapper userMapper, CoachMapper coachMapper,
                             com.driveschool.mapper.LearningPhaseMapper learningPhaseMapper) {
        this.vehicleTypeMapper = vehicleTypeMapper;
        this.subjectMapper = subjectMapper;
        this.examLocationMapper = examLocationMapper;
        this.studentInfoMapper = studentInfoMapper;
        this.userMapper = userMapper;
        this.coachMapper = coachMapper;
        this.learningPhaseMapper = learningPhaseMapper;
    }

    @GetMapping("/vehicle-types")
    public Result<?> listVehicleTypes() {
        return Result.ok(vehicleTypeMapper.selectList(
                new LambdaQueryWrapper<com.driveschool.entity.VehicleType>().eq(com.driveschool.entity.VehicleType::getEnabled, 1)));
    }

    @GetMapping("/subjects")
    public Result<?> listSubjects() {
        return Result.ok(subjectMapper.selectList(null));
    }

    @GetMapping("/exam-locations")
    public Result<?> listExamLocations() {
        return Result.ok(examLocationMapper.selectList(null));
    }

    /**
     * 获取教练名下学员列表。
     * 仅允许教练本人或管理员查看，防止越权访问。
     * 前端调用时不再传递 userId，后端从 JWT 中获取当前用户身份。
     */
    @GetMapping("/coaches/students")
    public Result<?> getCoachStudents() {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        String currentRole = SecurityUtils.getCurrentRole();
        if (currentUserId == null) {
            return Result.fail("未登录或登录已过期");
        }

        // 从 JWT 获取的 userId 即为当前教练的 userId（管理员也可查看）
        Long coachUserId;
        if ("ROLE_ADMIN".equals(currentRole)) {
            // 管理员需要传入要查看的教练userId（此处取当前登录用户自己的数据无意义，管理员通常通过 admin 接口管理）
            // 如果没有明确要查谁，返回空
            return Result.ok(Collections.emptyList());
        } else {
            coachUserId = currentUserId;
        }

        com.driveschool.entity.Coach coach = coachMapper.selectOne(
                new LambdaQueryWrapper<com.driveschool.entity.Coach>().eq(com.driveschool.entity.Coach::getUserId, coachUserId));
        if (coach == null) return Result.ok(Collections.emptyList());

        List<StudentInfo> students = studentInfoMapper.selectList(
                new LambdaQueryWrapper<StudentInfo>().eq(StudentInfo::getCoachId, coach.getId()));
        List<Map<String, Object>> result = new ArrayList<>();
        for (StudentInfo si : students) {
            Map<String, Object> map = new HashMap<>();
            map.put("studentInfo", si);
            User u = userMapper.selectById(si.getUserId());
            map.put("realName", u != null ? u.getRealName() : "");
            map.put("phone", u != null ? u.getPhone() : "");
            com.driveschool.entity.LearningPhase phase = learningPhaseMapper.selectOne(
                    new LambdaQueryWrapper<com.driveschool.entity.LearningPhase>()
                            .eq(com.driveschool.entity.LearningPhase::getStudentId, si.getId()));
            map.put("learningPhase", phase);
            result.add(map);
        }
        return Result.ok(result);
    }
}
