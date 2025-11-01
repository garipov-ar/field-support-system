package com.aidar.fieldsupport.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public class DocumentUploadDto {

    @NotBlank private String title;
    @NotBlank private String category;
    @NotBlank private String equipment;
    @NotBlank private String version;
    @NotBlank private String description;
    @NotNull private MultipartFile file;

    // Геттеры
    public String getTitle() { return title; }
    public String getCategory() { return category; }
    public String getEquipment() { return equipment; }
    public String getVersion() { return version; }
    public String getDescription() { return description; }
    public MultipartFile getFile() { return file; }

    // Сеттеры (обязательны!)
    public void setTitle(String title) { this.title = title; }
    public void setCategory(String category) { this.category = category; }
    public void setEquipment(String equipment) { this.equipment = equipment; }
    public void setVersion(String version) { this.version = version; }
    public void setDescription(String description) { this.description = description; }
    public void setFile(MultipartFile file) { this.file = file; }
}