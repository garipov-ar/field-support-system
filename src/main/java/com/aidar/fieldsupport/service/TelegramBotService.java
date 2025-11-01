package com.aidar.fieldsupport.service;

import com.aidar.fieldsupport.entity.Document;
import com.aidar.fieldsupport.entity.UserPreference;
import com.aidar.fieldsupport.repository.DocumentRepository;
import com.aidar.fieldsupport.repository.UserPreferenceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Slf4j
@Service
public class TelegramBotService extends TelegramLongPollingBot {

    @Value("${TELEGRAM_BOT_TOKEN}")
    private String botToken;

    @Value("${TELEGRAM_BOT_USERNAME}")
    private String botUsername;

    private final DocumentRepository documentRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final Path uploadPath;

    // Допустимые категории
    private static final Set<String> VALID_CATEGORIES = Set.of("FIRMWARE", "INSTRUCTION", "CONFIG", "PRICE");

    public TelegramBotService(
            DocumentRepository documentRepository,
            UserPreferenceRepository userPreferenceRepository,
            Path uploadPath) {
        this.documentRepository = documentRepository;
        this.userPreferenceRepository = userPreferenceRepository;
        this.uploadPath = uploadPath;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            if (message.hasText()) {
                Long chatId = message.getChatId();
                String text = message.getText();

                log.info("Received from {}: {}", chatId, text);

                if (text.equals("/start")) {
                    sendConsentMessage(chatId);
                    return;
                }

                if (text.equals("/agree")) {
                    saveConsent(chatId);
                    sendText(chatId, "✅ Согласие получено! Теперь вы можете пользоваться системой.");
                    sendHelp(chatId);
                    return;
                }

                // Проверка согласия
                if (!hasConsent(chatId)) {
                    sendConsentMessage(chatId);
                    return;
                }

                // Обработка команд
                if (text.startsWith("/search ")) {
                    handleSearch(chatId, text.substring(8).trim());
                } else if (text.equals("/firmware")) {
                    handleCategory(chatId, "FIRMWARE");
                } else if (text.equals("/instructions")) {
                    handleCategory(chatId, "INSTRUCTION");
                } else if (text.equals("/configs")) {
                    handleCategory(chatId, "CONFIG");
                } else if (text.equals("/price")) {
                    handleCategory(chatId, "PRICE");
                } else if (text.startsWith("/subscribe ")) {
                    handleSubscribe(chatId, text.substring(11).trim());
                } else if (text.equals("/share")) {
                    sendText(chatId, "📎 Чтобы поделиться документом, найдите его через /search и нажмите «Переслать» в Telegram.");
                } else {
                    sendHelp(chatId);
                }
            }
        }
    }

    private void sendConsentMessage(Long chatId) {
        String consent = """
                📢 Внимание!
                Для работы с системой требуется ваше согласие на обработку персональных данных.
                
                Мы собираем только ваш Telegram ID для идентификации.
                Данные не передаются третьим лицам и хранятся в защищённой базе.
                
                Нажмите /agree, чтобы дать согласие и продолжить.
                """;
        sendText(chatId, consent);
    }

    private void sendHelp(Long chatId) {
        String help = """
                Доступные команды:
                /search <ключ> — поиск по оборудованию или названию
                /firmware — прошивки
                /instructions — инструкции
                /configs — конфигурации
                /price — прайс-листы
                /subscribe <категория> — подписка на уведомления (firmware, instruction, config, price)
                /share — как поделиться документом
                """;
        sendText(chatId, help);
    }

    private boolean hasConsent(Long chatId) {
        return userPreferenceRepository.findByTelegramId(chatId)
                .map(UserPreference::isConsentGiven)
                .orElse(false);
    }

    private void saveConsent(Long chatId) {
        UserPreference pref = userPreferenceRepository.findByTelegramId(chatId)
                .orElse(new UserPreference());
        pref.setTelegramId(chatId);
        pref.setConsentGiven(true);
        userPreferenceRepository.save(pref);
    }

    private void handleSearch(Long chatId, String query) {
        if (query.isEmpty()) {
            sendText(chatId, "Укажите ключевые слова после /search");
            return;
        }
        List<Document> docs = documentRepository.findByEquipmentContainingIgnoreCaseAndIsCurrentTrue(query);
        if (docs.isEmpty()) {
            sendText(chatId, "Ничего не найдено.");
        } else {
            sendDocuments(chatId, docs);
        }
    }

    private void handleCategory(Long chatId, String category) {
        List<Document> docs = documentRepository.findByCategoryAndIsCurrentTrue(category);
        if (docs.isEmpty()) {
            sendText(chatId, "Документы не найдены.");
        } else {
            sendDocuments(chatId, docs);
        }
    }

    private void handleSubscribe(Long chatId, String categoryInput) {
        String category = categoryInput.toUpperCase();
        if (!VALID_CATEGORIES.contains(category)) {
            sendText(chatId, "Недопустимая категория. Доступны: firmware, instruction, config, price");
            return;
        }

        UserPreference pref = userPreferenceRepository.findByTelegramId(chatId)
                .orElse(new UserPreference());
        pref.setTelegramId(chatId);
        if (!pref.isConsentGiven()) {
            pref.setConsentGiven(true);
        }

        List<String> subs = new ArrayList<>(pref.getSubscribedCategories());
        if (!subs.contains(category)) {
            subs.add(category);
            pref.setSubscribedCategories(subs);
            userPreferenceRepository.save(pref);
            sendText(chatId, "✅ Вы подписаны на уведомления по категории: " + category);
        } else {
            sendText(chatId, "Вы уже подписаны на эту категорию.");
        }
    }

    private void sendDocuments(Long chatId, List<Document> docs) {
        for (Document doc : docs) {
            try {
                Path filePath = uploadPath.resolve(doc.getInternalFileName());
                if (!Files.exists(filePath)) {
                    sendText(chatId, "Файл не найден: " + doc.getFileName());
                    continue;
                }

                SendDocument sendDoc = new SendDocument();
                sendDoc.setChatId(chatId.toString());
                sendDoc.setDocument(new InputFile(filePath.toFile(), doc.getFileName()));
                sendDoc.setCaption(String.format(
                        "📄 %s\n🏷️ %s\n | 🖥️ %s\n | 🔢 %s",
                        doc.getTitle(),
                        doc.getCategory(),
                        doc.getEquipment(),
                        doc.getVersion()
                ));

                execute(sendDoc);
            } catch (Exception e) {
                log.error("Ошибка отправки файла пользователю {}", chatId, e);
                sendText(chatId, "Не удалось отправить файл: " + doc.getFileName());
            }
        }
    }

    private void sendText(Long chatId, String text) {
        try {
            execute(new SendMessage(chatId.toString(), text));
        } catch (TelegramApiException e) {
            log.error("Ошибка отправки сообщения пользователю {}", chatId, e);
        }
    }
}