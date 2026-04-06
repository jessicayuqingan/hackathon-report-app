package com.legacy.report.controller;

import com.legacy.report.dto.LoginRequest;
import com.legacy.report.dto.LoginResponse;
import com.legacy.report.dto.UserDto;
import com.legacy.report.model.User;
import com.legacy.report.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        String token = authService.login(request.getUsername(), request.getPassword());
        User user = authService.findByUsername(request.getUsername());
        UserDto userDto = UserDto.fromEntity(user);
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUser(userDto);
        return response;
    }

    @GetMapping("/profile")
    public UserDto profile(Principal principal) {
        if (principal == null) {
            throw new RuntimeException("Unauthorized");
        }
        User user = authService.findByUsername(principal.getName());
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        return UserDto.fromEntity(user);
    }

    @PostMapping("/logout")
    public Map<String, String> logout() {
        return Map.of("message", "Logged out successfully");
    }
}
