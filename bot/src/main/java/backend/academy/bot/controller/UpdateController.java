package backend.academy.bot.controller;

import backend.academy.bot.model.dto.request.LinkUpdateRequest;
import backend.academy.bot.util.UpdateFormatter;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class UpdateController {

    private final TelegramBot telegramBot;

    @Autowired
    public UpdateController(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    @PostMapping("/updates")
    public ResponseEntity<String> update(@RequestBody LinkUpdateRequest request) {
        request.tgChatIds()
                .forEach(
                        chatId -> telegramBot.execute(new SendMessage(chatId, UpdateFormatter.formatMessage(request))));
        return ResponseEntity.ok("Обновление обработано");
    }
}
