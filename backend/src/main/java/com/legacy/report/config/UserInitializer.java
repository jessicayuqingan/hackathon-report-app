package com.legacy.report.config;

import com.legacy.report.model.User;
import com.legacy.report.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class UserInitializer {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostConstruct
    public void init() {
        if (!userRepository.existsByUsername("admin")) {
            User user = new User();
            user.setUsername("admin");
            user.setPassword(passwordEncoder.encode("123456"));
            user.setRole("MAKER,CHECKER");
            userRepository.save(user);
        }

        if (!userRepository.existsByUsername("maker1")) {
            User maker = new User();
            maker.setUsername("maker1");
            maker.setPassword(passwordEncoder.encode("123456"));
            maker.setRole("MAKER");
            userRepository.save(maker);
        }

        if (!userRepository.existsByUsername("checker1")) {
            User checker = new User();
            checker.setUsername("checker1");
            checker.setPassword(passwordEncoder.encode("123456"));
            checker.setRole("CHECKER");
            userRepository.save(checker);
        }
    }
}
