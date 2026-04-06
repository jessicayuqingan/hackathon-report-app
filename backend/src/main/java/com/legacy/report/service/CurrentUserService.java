package com.legacy.report.service;

import com.legacy.report.model.User;
import com.legacy.report.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class CurrentUserService {

    @Autowired
    private UserRepository userRepository;

    public User getCurrentUserOrThrow() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("未找到认证用户");
        }
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + username));
    }

    public boolean hasRole(User user, String requiredRole) {
        if (user == null || user.getRole() == null) {
            return false;
        }
        String[] roles = user.getRole().split(",");
        return Arrays.stream(roles)
                .map(String::trim)
                .anyMatch(r -> r.equalsIgnoreCase(requiredRole));
    }

    public void requireRole(User user, String requiredRole) {
        if (!hasRole(user, requiredRole)) {
            throw new RuntimeException("当前用户没有所需角色: " + requiredRole);
        }
    }
}
