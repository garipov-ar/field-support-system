package com.aidar.fieldsupport.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "documents", indexes = {
        @Index(name = "idx_category", columnList = "category"),
        @Index(name = "idx_equipment", columnList = "equipment"),
        @Index(name = "idx_is_current", columnList = "is_current")
})
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String category; // INSTRUCTION, FIRMWARE, CONFIG, PRICE

    @Column(nullable = false)
    private String equipment;

    @Column(nullable = false)
    private String version;

    @Column(nullable = false)
    private String fileName; // original name

    @Column(nullable = false)
    private String internalFileName; // unique name on disk

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private boolean isCurrent = true;

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    @Column(nullable = false)
    private String updatedBy;

    // Getters
    public UUID getId() { return id; }
    public String getTitle() { return title; }
    public String getCategory() { return category; }
    public String getEquipment() { return equipment; }
    public String getVersion() { return version; }
    public String getFileName() { return fileName; }
    public String getInternalFileName() { return internalFileName; }
    public String getDescription() { return description; }
    public boolean isCurrent() { return isCurrent; }
    public Instant getUpdatedAt() { return updatedAt; }
    public String getUpdatedBy() { return updatedBy; }

    // Setters
    public void setId(UUID id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setCategory(String category) { this.category = category; }
    public void setEquipment(String equipment) { this.equipment = equipment; }
    public void setVersion(String version) { this.version = version; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public void setInternalFileName(String internalFileName) { this.internalFileName = internalFileName; }
    public void setDescription(String description) { this.description = description; }
    public void setCurrent(boolean current) { isCurrent = current; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}