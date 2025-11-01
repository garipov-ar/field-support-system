package com.aidar.fieldsupport.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "documents", indexes = {
        @Index(name = "idx_category", columnList = "category"),
        @Index(name = "idx_equipment", columnList = "equipment"),
        @Index(name = "idx_status", columnList = "status")
})
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title; // Название документа

    @Column(nullable = false)
    private String category; // "INSTRUCTION", "FIRMWARE", "CONFIG", "PRICE"

    @Column(nullable = false)
    private String equipment; // Модель оборудования (например, "XG-200")

    @Column(nullable = false)
    private String version; // Версия прошивки/документа

    @Column(nullable = false)
    private String fileName; // Имя файла на диске или в облаке

    @Column(columnDefinition = "TEXT")
    private String description; // Описание

    @Column(nullable = false)
    private boolean isCurrent = true; // true = актуальный, false = архивный

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    @Column(nullable = false)
    private String updatedBy; // Кто обновил (логин или Telegram ID)

    // Геттеры и сеттеры
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getEquipment() { return equipment; }
    public void setEquipment(String equipment) { this.equipment = equipment; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isCurrent() { return isCurrent; }
    public void setCurrent(boolean current) { isCurrent = current; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}