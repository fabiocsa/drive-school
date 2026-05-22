package com.driveschool.controller;

import com.driveschool.entity.User;
import com.driveschool.service.UserService;
import com.driveschool.util.Result;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public Result<?> login(@RequestBody Map<String, String> loginData) {
        String username = loginData.get("username");
        String password = loginData.get("password");
        Map<String, Object> result = userService.login(username, password);
        return Result.ok(result);
    }

    @PostMapping("/register")
    public Result<?> register(@RequestBody User user) {
        User saved = userService.register(user);
        return Result.ok(saved);
    }
}
