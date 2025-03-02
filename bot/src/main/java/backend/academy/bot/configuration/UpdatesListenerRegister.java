package backend.academy.bot.configuration;

import backend.academy.bot.exceptions.NoSuchCommandException;
import backend.academy.bot.exceptions.UnavaliableCommandException;
import backend.academy.bot.exceptions.UnregisteredException;
import backend.academy.bot.model.command.Command;
import backend.academy.bot.service.CommandManager;
import backend.academy.bot.service.CommandParser;
import backend.academy.bot.service.impl.StateMachineImpl;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.request.SendMessage;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UpdatesListenerRegister {

    private final TelegramBot telegramBot;

    private final CommandManager commandManager;

    private final CommandParser commandParser;

    private final StateMachineImpl stateMachine;

    @Autowired
    public UpdatesListenerRegister(
            TelegramBot telegramBot,
            CommandManager commandManager,
            CommandParser commandParser,
            StateMachineImpl stateMachine) {
        this.telegramBot = telegramBot;
        this.commandManager = commandManager;
        this.commandParser = commandParser;
        this.stateMachine = stateMachine;
    }

    @PostConstruct
    public void register() {
        telegramBot.setUpdatesListener(updates -> {
            updates.forEach(update -> {
                if (update.message() != null && update.message().text() != null) {
                    if (update.message().text().startsWith("/")) {
                        long chatId = update.message().chat().id();
                        String text = update.message().text();
                        try {
                            Command command = commandParser.getCommand(text, update);
                            if (command != null) {
                                telegramBot.execute(new SendMessage(chatId, commandManager.executeCommand(command)));
                            }
                        } catch (NoSuchCommandException | UnregisteredException | UnavaliableCommandException e) {
                            telegramBot.execute(new SendMessage(chatId, e.getMessage()));
                        }
                    } else {
                        long chatId = update.message().chat().id();
                        Command command = stateMachine.process(chatId, update);
                        if (command != null) {
                            telegramBot.execute(new SendMessage(chatId, commandManager.executeCommand(command)));
                        }
                    }
                }
            });
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }
}
