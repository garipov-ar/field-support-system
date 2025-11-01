package com.aidar.fieldsupport.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user_preferences", indexes = {
        @Index(name = "idx_telegram_id", columnList = "telegram_id", unique = true)
})
public class UserPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "telegram_id", nullable = false, unique = true)
    private Long telegramId;

    @Column(name = "consent_given", nullable = false)
    private boolean consentGiven = false;

    @Column(name = "subscribed_categories", columnDefinition = "TEXT")
    private String subscribedCategoriesJson = "[]"; // JSON-массив строк

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTelegramId() { return telegramId; }
    public void setTelegramId(Long telegramId) { this.telegramId = telegramId; }

    public boolean isConsentGiven() { return consentGiven; }
    public void setConsentGiven(boolean consentGiven) { this.consentGiven = consentGiven; }

    public String getSubscribedCategoriesJson() { return subscribedCategoriesJson; }
    public void setSubscribedCategoriesJson(String subscribedCategoriesJson) { this.subscribedCategoriesJson = subscribedCategoriesJson; }

    // Вспомогательные методы
    public List<String> getSubscribedCategories() {
        // Простой парсинг JSON-массива (для MVP)
        if (subscribedCategoriesJson == null || subscribedCategoriesJson.equals("[]")) {
            return new ArrayList<>();
        }
        // Убираем [ и ], разбиваем по запятым и убираем кавычки
        return java.util.Arrays.stream(
                        subscribedCategoriesJson
                                .replace("[", "")
                                .replace("]", "")
                                .split(",")
                )
                .map(String::trim)
                .map(s -> s.replace("\"", ""))
                .filter(s -> !s.isEmpty())
                .toList();
    }

    public void setSubscribedCategories(List<String> categories) {
        if (categories == null || categories.isEmpty()) {
            this.subscribedCategoriesJson = "[]";
        } else {
            this.subscribedCategoriesJson = "[\"" + String.join("\",\"", categories) + "\"]";
        }
    }
}