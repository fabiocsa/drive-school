package com.driveschool.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.driveschool.entity.Coach;
import com.driveschool.entity.StudentInfo;
import com.driveschool.entity.User;
import com.driveschool.mapper.CoachMapper;
import com.driveschool.mapper.StudentInfoMapper;
import com.driveschool.mapper.UserMapper;
import com.driveschool.service.CoachService;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CoachServiceImpl extends ServiceImpl<CoachMapper, Coach> implements CoachService {

    private final UserMapper userMapper;
    private final StudentInfoMapper studentInfoMapper;

    public CoachServiceImpl(UserMapper userMapper, StudentInfoMapper studentInfoMapper) {
        this.userMapper = userMapper;
        this.studentInfoMapper = studentInfoMapper;
    }

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
            if (count >= 3) break;
            Map<String, Object> map = new HashMap<>();
            User user = userMapper.selectById(coach.getUserId());
            map.put("coach", coach);
            map.put("name", user != null ? user.getRealName() : "");
            map.put("phone", user != null ? user.getPhone() : "");
            result.add(map);
            count++;
        }
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
}
