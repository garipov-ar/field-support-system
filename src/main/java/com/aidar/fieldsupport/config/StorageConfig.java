package com.aidar.fieldsupport.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;

@Configuration
public class StorageConfig {

    @Value("${app.storage.upload-dir:${user.dir}/uploads}")
    private String uploadDir;

    @Bean
    public Path uploadPath() {
        return Path.of(uploadDir).toAbsolutePath().normalize();
    }
}