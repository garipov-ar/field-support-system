package com.aidar.fieldsupport.controller;

import com.aidar.fieldsupport.dto.DocumentUploadDto;
import com.aidar.fieldsupport.entity.Document;
import com.aidar.fieldsupport.entity.User;
import com.aidar.fieldsupport.repository.DocumentRepository;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
public class CmsController {

    private final DocumentRepository documentRepository;
    private final Path uploadPath;

    public CmsController(DocumentRepository documentRepository, Path uploadPath) {
        this.documentRepository = documentRepository;
        this.uploadPath = uploadPath;
        try {
            Files.createDirectories(this.uploadPath);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать директорию для загрузок", e);
        }
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("documents", documentRepository.findAll());
        return "dashboard";
    }

    @GetMapping("/upload")
    public String uploadForm(Model model) {
        model.addAttribute("document", new DocumentUploadDto());
        return "upload";
    }

    @PostMapping("/upload")
    public String uploadDocument(@Valid DocumentUploadDto dto, BindingResult bindingResult, Model model) throws IOException {
        if (bindingResult.hasErrors()) {
            model.addAttribute("errors", "Все поля обязательны");
            return "upload";
        }

        MultipartFile file = dto.getFile();
        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isEmpty()) originalName = "unnamed";
        String internalName = UUID.randomUUID() + "-" + originalName;

        Path filePath = uploadPath.resolve(internalName);
        Files.copy(file.getInputStream(), filePath);

        Document doc = new Document();
        doc.setTitle(dto.getTitle());
        doc.setCategory(dto.getCategory().toUpperCase());
        doc.setEquipment(dto.getEquipment());
        doc.setVersion(dto.getVersion());
        doc.setFileName(originalName);
        doc.setInternalFileName(internalName);
        doc.setDescription(dto.getDescription());
        doc.setCurrent(true);
        doc.setUpdatedBy("admin"); // TODO: брать из Authentication
        doc.setUpdatedAt(java.time.Instant.now());

        documentRepository.save(doc);
        return "redirect:/admin/dashboard?success";
    }

    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("allRoles", List.of("SPECIALIST", "SUPPORT", "ADMIN"));
        return "users";
    }

    @PostMapping("/users")
    public String createUser(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam List<String> roles) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRoles(roles); // например: ["SUPPORT", "ADMIN"]
        userRepository.save(user);
        return "redirect:/admin/users";
    }

    @PostMapping("/toggle-status/{id}")
    public String toggleStatus(@PathVariable UUID id) {
        Document doc = documentRepository.findById(id).orElseThrow();
        doc.setCurrent(!doc.isCurrent());
        documentRepository.save(doc);
        return "redirect:/admin/dashboard";
    }
}