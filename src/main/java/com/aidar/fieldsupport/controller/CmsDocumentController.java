package com.aidar.fieldsupport.controller;

import com.aidar.fieldsupport.entity.Document;
import com.aidar.fieldsupport.repository.DocumentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cms/documents")
public class CmsDocumentController {

    private final DocumentRepository documentRepository;

    public CmsDocumentController(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    @PostMapping
    public ResponseEntity<?> uploadDocument(@RequestBody DocumentUploadRequest request) {
        Document doc = new Document();
        doc.setTitle(request.getTitle());
        doc.setCategory(request.getCategory().toUpperCase());
        doc.setEquipment(request.getEquipment());
        doc.setVersion(request.getVersion());
        doc.setFileName(request.getFileName()); // Например, "xg200_firmware_v1.2.zip"
        doc.setDescription(request.getDescription());
        doc.setCurrent(true);
        doc.setUpdatedBy("admin"); // Позже заменим на реального пользователя
        doc.setUpdatedAt(java.time.Instant.now());

        documentRepository.save(doc);
        return ResponseEntity.ok().build();
    }

    // Вложенный класс для приёма JSON
    public static class DocumentUploadRequest {
        private String title;
        private String category; // "firmware", "instruction" и т.д.
        private String equipment;
        private String version;
        private String fileName;
        private String description;

        // Геттеры
        public String getTitle() { return title; }
        public String getCategory() { return category; }
        public String getEquipment() { return equipment; }
        public String getVersion() { return version; }
        public String getFileName() { return fileName; }
        public String getDescription() { return description; }
    }
}