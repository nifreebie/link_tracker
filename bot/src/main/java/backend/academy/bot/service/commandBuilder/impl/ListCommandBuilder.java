package backend.academy.bot.service.commandBuilder.impl;

import backend.academy.bot.exceptions.UnavaliableCommandException;
import backend.academy.bot.exceptions.UnregisteredException;
import backend.academy.bot.model.UserState;
import backend.academy.bot.model.command.Command;
import backend.academy.bot.model.command.impl.ListCommand;
import backend.academy.bot.repository.StateRepository;
import backend.academy.bot.service.commandBuilder.CommandBuilder;
import backend.academy.bot.util.BotMessages;
import com.pengrad.telegrambot.model.Update;
import org.springframework.stereotype.Component;

@Component
public class ListCommandBuilder extends CommandBuilder implements BotMessages {

    protected ListCommandBuilder(StateRepository stateRepository) {
        super(stateRepository);
    }

    @Override
    public Command build(Update update) {
        UserState state = stateRepository.getStateById(update.message().chat().id());
        if (state == null) {
            throw new UnregisteredException(REGISTRATION_NEED);
        }
        if (state != UserState.DEFAULT) {
            throw new UnavaliableCommandException(COMMAND_NOT_ALLOWED);
        }
        return new ListCommand(update.message().chat().id());
    }
}
