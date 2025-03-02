package backend.academy.bot.service.commandBuilder.impl;

import backend.academy.bot.exceptions.UnavaliableCommandException;
import backend.academy.bot.exceptions.UnregisteredException;
import backend.academy.bot.model.UserState;
import backend.academy.bot.model.command.Command;
import backend.academy.bot.repository.StateRepository;
import backend.academy.bot.service.commandBuilder.CommandBuilder;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.stereotype.Component;

@Component
public class UntrackCommandBuilder extends CommandBuilder {
    private final TelegramBot telegramBot;

    protected UntrackCommandBuilder(StateRepository stateRepository, TelegramBot telegramBot) {
        super(stateRepository);
        this.telegramBot = telegramBot;
    }

    @Override
    public Command build(Update update) {
        UserState state = stateRepository.getStateById(update.message().chat().id());
        if (state == null) {
            throw new UnregisteredException("Сначала нужно зарегистрироваться, нажмите /start");
        }
        if (state != UserState.DEFAULT) {
            throw new UnavaliableCommandException("Сейчас вы не можете использовать эту команду");
        }
        stateRepository.setState(update.message().chat().id(), UserState.AWAITING_UNTRACK_URL);
        telegramBot.execute(new SendMessage(update.message().chat().id(), "Введите ссылку"));
        return null;
    }
}
