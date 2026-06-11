package com.driveschool.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全工具类，从 SecurityContext 获取当前登录用户信息
 */
public class SecurityUtils {

    /**
     * 获取当前登录用户的 ID
     */
    public static Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getCredentials() instanceof Long) {
            return (Long) auth.getCredentials();
        }
        return null;
    }

    /**
     * 获取当前登录用户的用户名
     */
    public static String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            return auth.getName();
        }
        return null;
    }

    /**
     * 获取当前登录用户的角色
     */
    public static String getCurrentRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getAuthorities() != null && !auth.getAuthorities().isEmpty()) {
            return auth.getAuthorities().iterator().next().getAuthority();
        }
        return null;
    }
}
