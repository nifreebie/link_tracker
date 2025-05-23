package backend.academy.bot.configuration;

import backend.academy.bot.exceptions.NoSuchCommandException;
import backend.academy.bot.exceptions.UnavailableCommandException;
import backend.academy.bot.exceptions.UnregisteredException;
import backend.academy.bot.model.command.Command;
import backend.academy.bot.service.CommandManager;
import backend.academy.bot.service.CommandParser;
import backend.academy.bot.service.impl.StateMachineImpl;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UpdatesListenerRegister {

    private final TelegramBot telegramBot;

    private final CommandManager commandManager;

    private final CommandParser commandParser;

    private final StateMachineImpl stateMachine;

    @PostConstruct
    public void register() {
        telegramBot.setUpdatesListener(updates -> {
            for (Update update : updates) {
                if (update.message() == null && update.callbackQuery() == null) {
                    continue;
                }

                Long chatId;
                if (update.message() != null) {
                    Message msg = update.message();
                    chatId = msg.chat().id();

                    String text = msg.text();
                    if (text == null) {
                        continue;
                    }

                    if (text.startsWith("/")) {
                        try {
                            Command command = commandParser.getCommand(text, update);
                            if (command != null) {
                                String result = commandManager.executeCommand(command);
                                telegramBot.execute(new SendMessage(chatId, result));
                            }
                        } catch (NoSuchCommandException | UnregisteredException | UnavailableCommandException e) {
                            telegramBot.execute(new SendMessage(chatId, e.getMessage()));
                        }
                        continue;
                    }

                    Command stateCommand = stateMachine.process(chatId, update);
                    if (stateCommand != null) {
                        String result = commandManager.executeCommand(stateCommand);
                        telegramBot.execute(new SendMessage(chatId, result));
                    }
                    continue;
                }
                CallbackQuery cq = update.callbackQuery();
                chatId = cq.message().chat().id();

                Command callbackCommand = stateMachine.process(chatId, update);
                if (callbackCommand != null) {
                    String result = commandManager.executeCommand(callbackCommand);
                    telegramBot.execute(new SendMessage(chatId, result));
                }
            }

            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }
}
