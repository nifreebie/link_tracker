package backend.academy.bot.service.impl;

import backend.academy.bot.model.dto.request.LinkUpdateRequest;
import backend.academy.bot.service.UpdateService;
import backend.academy.bot.util.UpdateFormatter;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UpdateServiceImpl implements UpdateService {
    private final TelegramBot telegramBot;

    @Autowired
    public UpdateServiceImpl(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    @Override
    public void sendUpdates(LinkUpdateRequest request) {
        request.tgChatIds()
                .forEach(
                        chatId -> telegramBot.execute(new SendMessage(chatId, UpdateFormatter.formatMessage(request))));
    }
}
