package com.driveschool.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.driveschool.entity.Coach;
import com.driveschool.entity.User;
import com.driveschool.mapper.CoachMapper;
import com.driveschool.mapper.UserMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 数据库初始化器 —— 仅负责动态用户数据。
 *
 * 分工：
 *   schema.sql   → DDL 建表（CREATE TABLE IF NOT EXISTS，幂等）
 *   data.sql     → 静态基础数据：车型、科目、考场、费用标准（INSERT IGNORE，幂等）
 *   本类         → 动态用户数据：admin/coach/student，使用 BCrypt 编码密码
 *
 * 所有 initUserIfAbsent 调用均有存在性检查，重复启动安全。
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserMapper userMapper;
    private final CoachMapper coachMapper;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserMapper userMapper, CoachMapper coachMapper,
                           PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.coachMapper = coachMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        initUserIfAbsent("admin", "123456", "ROLE_ADMIN", "系统管理员", "13800000000");

        Long coach1Id = initUserIfAbsent("coach1", "123456", "ROLE_COACH", "张教练", "13800000001");
        Long coach2Id = initUserIfAbsent("coach2", "123456", "ROLE_COACH", "李教练", "13800000002");
        Long coach3Id = initUserIfAbsent("coach3", "123456", "ROLE_COACH", "王教练", "13800000003");

        initUserIfAbsent("student1", "123456", "ROLE_STUDENT", "张三", "13900000001");
        initUserIfAbsent("student2", "123456", "ROLE_STUDENT", "李四", "13900000002");

        // 使用新格式 schedule_json：每项含 dayTime + capacity
        initCoachIfAbsent(coach1Id, "男", 1L, 5,
                "[{\"dayTime\":\"周一上午\",\"capacity\":5},{\"dayTime\":\"周一下午\",\"capacity\":5},{\"dayTime\":\"周二上午\",\"capacity\":4},{\"dayTime\":\"周三上午\",\"capacity\":5},{\"dayTime\":\"周四上午\",\"capacity\":4},{\"dayTime\":\"周五上午\",\"capacity\":5}]");
        initCoachIfAbsent(coach2Id, "女", 1L, 4,
                "[{\"dayTime\":\"周二下午\",\"capacity\":3},{\"dayTime\":\"周三下午\",\"capacity\":3},{\"dayTime\":\"周四下午\",\"capacity\":3},{\"dayTime\":\"周五下午\",\"capacity\":4},{\"dayTime\":\"周六上午\",\"capacity\":4}]");
        initCoachIfAbsent(coach3Id, "男", 2L, 4,
                "[{\"dayTime\":\"周一上午\",\"capacity\":4},{\"dayTime\":\"周三上午\",\"capacity\":4},{\"dayTime\":\"周五上午\",\"capacity\":3},{\"dayTime\":\"周日上午\",\"capacity\":5}]");
    }

    private Long initUserIfAbsent(String username, String password, String role,
                                   String realName, String phone) {
        User existing = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (existing == null) {
            User user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(password));
            user.setRole(role);
            user.setRealName(realName);
            user.setPhone(phone);
            user.setEnabled(1);
            userMapper.insert(user);
            return user.getId();
        }
        return existing.getId();
    }

    private void initCoachIfAbsent(Long userId, String gender, Long vehicleTypeId,
                                    Integer rating, String scheduleJson) {
        Coach existing = coachMapper.selectOne(
                new LambdaQueryWrapper<Coach>().eq(Coach::getUserId, userId));
        if (existing == null) {
            Coach coach = new Coach();
            coach.setUserId(userId);
            coach.setGender(gender);
            coach.setVehicleTypeId(vehicleTypeId);
            coach.setRating(rating);
            coach.setScheduleJson(scheduleJson);
            coachMapper.insert(coach);
        }
    }
}
