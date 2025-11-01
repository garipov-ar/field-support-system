package com.aidar.fieldsupport.config;

import com.aidar.fieldsupport.entity.User;
import com.aidar.fieldsupport.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            User admin = new User();
            admin.setUsername("admin@example.com");
            admin.setPassword(passwordEncoder.encode("admin123")); // хэшируем пароль
            admin.setRoles(List.of("ADMIN", "SUPPORT")); // роли без префикса ROLE_
            userRepository.save(admin);
        }
    }
}
