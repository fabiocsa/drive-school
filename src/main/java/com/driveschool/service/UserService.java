package com.driveschool.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.driveschool.entity.*;

import java.util.Map;

public interface UserService extends IService<User> {
    Map<String, Object> login(String username, String password);
    User register(User user);
    User findByUsername(String username);
}
