package com.aidar.fieldsupport;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FieldSupportSystemApplication {
    public static void main(String[] args) {
        // Загружаем .env (если есть)
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        System.setProperty("TELEGRAM_BOT_TOKEN", dotenv.get("TELEGRAM_BOT_TOKEN"));
        System.setProperty("TELEGRAM_BOT_USERNAME", dotenv.get("TELEGRAM_BOT_USERNAME"));
        SpringApplication.run(FieldSupportSystemApplication.class, args);
    }
}