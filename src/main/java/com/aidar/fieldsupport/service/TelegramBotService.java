package com.aidar.fieldsupport.service;

import com.aidar.fieldsupport.entity.Document;
import com.aidar.fieldsupport.repository.DocumentRepository;
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
import java.util.List;

@Slf4j
@Service
public class TelegramBotService extends TelegramLongPollingBot {

    @Value("${TELEGRAM_BOT_TOKEN}")
    private String botToken;

    @Value("${TELEGRAM_BOT_USERNAME}")
    private String botUsername;

    private final DocumentRepository documentRepository;

    public TelegramBotService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
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
                String text = message.getText();
                Long chatId = message.getChatId();

                log.info("Received from {}: {}", chatId, text);

                if (text.startsWith("/search ")) {
                    handleSearch(chatId, text.substring(8).trim());
                } else if (text.equals("/firmware")) {
                    handleCategory(chatId, "FIRMWARE");
                } else if (text.equals("/instructions")) {
                    handleCategory(chatId, "INSTRUCTION");
                } else if (text.equals("/price")) {
                    handleCategory(chatId, "PRICE");
                } else {
                    sendText(chatId, """
                            Доступные команды:
                            /search <ключ> — поиск по ключевым словам
                            /firmware — прошивки
                            /instructions — инструкции
                            /price — прайс-листы
                            """);
                }
            }
        }
    }

    private void handleSearch(Long chatId, String query) {
        if (query.isEmpty()) {
            sendText(chatId, "Укажите ключевые слова после /search");
            return;
        }

        // Поиск по equipment и title (упрощённо)
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

    private void sendDocuments(Long chatId, List<Document> docs) {
        for (Document doc : docs) {
            try {
                // В реальном проекте: файлы хранятся на диске или в облаке
                // Сейчас отправим текстовое описание
                String caption = String.format("""
                        📄 %s
                        🏷️ Категория: %s
                        🖥️ Оборудование: %s
                        🔢 Версия: %s
                        """,
                        doc.getTitle(), doc.getCategory(), doc.getEquipment(), doc.getVersion());

                // Заглушка: отправляем текст вместо файла
                sendText(chatId, caption + "\n📎 Файл: " + doc.getFileName());
            } catch (Exception e) {
                log.error("Error sending document", e);
                sendText(chatId, "Ошибка при отправке документа.");
            }
        }
    }

    private void sendText(Long chatId, String text) {
        try {
            execute(new SendMessage(chatId.toString(), text));
        } catch (TelegramApiException e) {
            log.error("Error sending message", e);
        }
    }
}