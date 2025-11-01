package com.aidar.fieldsupport.controller;

import com.aidar.fieldsupport.entity.Document;
import com.aidar.fieldsupport.entity.User;
import com.aidar.fieldsupport.repository.DocumentRepository;
import com.aidar.fieldsupport.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
public class CmsController {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final Path uploadPath;

    public CmsController(
            DocumentRepository documentRepository,
            UserRepository userRepository,
            BCryptPasswordEncoder passwordEncoder,
            Path uploadPath) {
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
        return "upload";
    }

    @PostMapping("/upload")
    public String uploadDocument(
            @RequestParam String title,
            @RequestParam String category,
            @RequestParam String equipment,
            @RequestParam String version,
            @RequestParam String description,
            @RequestParam MultipartFile file,
            Authentication authentication,
            Model model) throws IOException {

        if (file.isEmpty()) {
            model.addAttribute("error", "Файл не выбран");
            return "upload";
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.trim().isEmpty()) {
            originalName = "unnamed";
        }
        String internalName = UUID.randomUUID() + "-" + originalName;

        Path filePath = uploadPath.resolve(internalName);
        Files.copy(file.getInputStream(), filePath);

        Document doc = new Document();
        doc.setTitle(title);
        doc.setCategory(category.toUpperCase());
        doc.setEquipment(equipment);
        doc.setVersion(version);
        doc.setFileName(originalName);
        doc.setInternalFileName(internalName);
        doc.setDescription(description);
        doc.setCurrent(true);
        doc.setUpdatedBy(authentication.getName());
        doc.setUpdatedAt(Instant.now());

        documentRepository.save(doc);
        return "redirect:/admin/dashboard?uploaded";
    }

    @PostMapping("/toggle-status/{id}")
    public String toggleStatus(@PathVariable UUID id) {
        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Документ не найден"));
        doc.setCurrent(!doc.isCurrent());
        documentRepository.save(doc);
        return "redirect:/admin/dashboard";
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
            @RequestParam List<String> roles,
            Model model) {
        if (userRepository.findByUsername(username).isPresent()) {
            model.addAttribute("error", "Пользователь уже существует");
            model.addAttribute("users", userRepository.findAll());
            model.addAttribute("allRoles", List.of("SPECIALIST", "SUPPORT", "ADMIN"));
            return "users";
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRoles(roles);
        userRepository.save(user);
        return "redirect:/admin/users?created";
    }
}