package backend.academy.bot.controller;

import backend.academy.bot.dto.request.LinkUpdateRequest;
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
                .forEach(chatId -> telegramBot.execute(
                        new SendMessage(chatId, "По ссылке " + request.url() + " новое обновление")));
        return ResponseEntity.ok("Обновление обработано");
    }
}
