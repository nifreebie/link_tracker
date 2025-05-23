package backend.academy.bot.service.commandBuilder.impl;

import backend.academy.bot.exceptions.UnavailableCommandException;
import backend.academy.bot.exceptions.UnregisteredException;
import backend.academy.bot.model.UserState;
import backend.academy.bot.model.command.Command;
import backend.academy.bot.repository.StateRepository;
import backend.academy.bot.service.commandBuilder.CommandBuilder;
import backend.academy.bot.util.BotMessages;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.stereotype.Component;

@Component
public class UntrackCommandBuilder extends CommandBuilder implements BotMessages {
    private final TelegramBot telegramBot;

    protected UntrackCommandBuilder(StateRepository stateRepository, TelegramBot telegramBot) {
        super(stateRepository);
        this.telegramBot = telegramBot;
    }

    @Override
    public Command build(Update update) {
        UserState state = stateRepository.getStateById(update.message().chat().id());
        if (state == null) {
            throw new UnregisteredException(REGISTRATION_NEED);
        }
        if (state != UserState.DEFAULT) {
            throw new UnavailableCommandException(COMMAND_NOT_ALLOWED);
        }
        stateRepository.setState(update.message().chat().id(), UserState.AWAITING_UNTRACK_URL);
        telegramBot.execute(new SendMessage(update.message().chat().id(), ENTER_LINK));
        return null;
    }
}
