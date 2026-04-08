package com.legacy.report.service;

import com.legacy.report.model.User;
import com.legacy.report.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

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

    public Set<Long> getAllowedReportIds(User user) {
        if (user == null || user.getReportAccessScope() == null || user.getReportAccessScope().isBlank()) {
            return Collections.emptySet();
        }
        String scope = user.getReportAccessScope().trim();
        if ("*".equals(scope)) {
            return Collections.emptySet();
        }
        return Arrays.stream(scope.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .map(Long::valueOf)
                .collect(Collectors.toSet());
    }

    public boolean hasReportAccess(User user, Long reportId) {
        Set<Long> allowedReportIds = getAllowedReportIds(user);
        return allowedReportIds.isEmpty() || allowedReportIds.contains(reportId);
    }

    public void requireReportAccess(User user, Long reportId) {
        if (!hasReportAccess(user, reportId)) {
            throw new RuntimeException("当前用户无权访问该报表: " + reportId);
        }
    }
}
