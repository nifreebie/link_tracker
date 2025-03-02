package backend.academy.bot.service.commandBuilder.impl;

import backend.academy.bot.exceptions.UnavaliableCommandException;
import backend.academy.bot.exceptions.UnregisteredException;
import backend.academy.bot.model.UserState;
import backend.academy.bot.model.command.Command;
import backend.academy.bot.model.command.impl.CancelCommand;
import backend.academy.bot.repository.StateRepository;
import backend.academy.bot.service.commandBuilder.CommandBuilder;
import com.pengrad.telegrambot.model.Update;
import org.springframework.stereotype.Component;

@Component
public class CancelCommandBuilder extends CommandBuilder {

    protected CancelCommandBuilder(StateRepository stateRepository) {
        super(stateRepository);
    }

    @Override
    public Command build(Update update) {
        UserState state = stateRepository.getStateById(update.message().chat().id());
        if (state == null) {
            throw new UnregisteredException("Сначала нужно зарегистрироваться, нажмите /start");
        }
        if (state == UserState.DEFAULT) {
            throw new UnavaliableCommandException("Сейчас вам нечего отменять");
        }
        return new CancelCommand(update.message().chat().id());
    }
}
