package com.aidar.fieldsupport.controller;

import com.aidar.fieldsupport.dto.DocumentUploadDto;
import com.aidar.fieldsupport.entity.Document;
import com.aidar.fieldsupport.repository.DocumentRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@RestController
@RequestMapping("/api/cms/documents")
public class CmsDocumentController {

    private static final Logger log = LoggerFactory.getLogger(CmsDocumentController.class);

    private final DocumentRepository documentRepository;
    private final Path uploadPath;

    public CmsDocumentController(DocumentRepository documentRepository, Path uploadPath) {
        this.documentRepository = documentRepository;
        this.uploadPath = uploadPath;
        try {
            Files.createDirectories(this.uploadPath);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать директорию для загрузок: " + uploadPath, e);
        }
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadDocument(@Valid @ModelAttribute DocumentUploadDto dto, BindingResult bindingResult) throws IOException {
        if (bindingResult.hasErrors()) {
            log.error("Ошибка валидации при загрузке документа: {}", bindingResult.getAllErrors());
            return ResponseEntity.badRequest().body("Ошибка валидации: все поля обязательны.");
        }

        MultipartFile file = dto.getFile();
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Файл не выбран.");
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.trim().isEmpty()) {
            originalName = "unnamed";
        }

        // Генерируем уникальное имя файла
        String internalName = UUID.randomUUID() + "-" + originalName;

        // Сохраняем файл на диск
        Path filePath = uploadPath.resolve(internalName);
        Files.copy(file.getInputStream(), filePath);

        // Сохраняем метаданные в БД
        Document doc = new Document();
        doc.setTitle(dto.getTitle());
        doc.setCategory(dto.getCategory().toUpperCase());
        doc.setEquipment(dto.getEquipment());
        doc.setVersion(dto.getVersion());
        doc.setFileName(originalName);
        doc.setInternalFileName(internalName);
        doc.setDescription(dto.getDescription());
        doc.setCurrent(true);
        doc.setUpdatedBy("admin");
        doc.setUpdatedAt(java.time.Instant.now());

        documentRepository.save(doc);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/download/{internalName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String internalName) throws IOException {
        Path filePath = uploadPath.resolve(internalName).normalize();
        Resource resource = new UrlResource(filePath.toUri());

        if (resource.exists()) {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + internalName + "\"")
                    .body(resource);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}