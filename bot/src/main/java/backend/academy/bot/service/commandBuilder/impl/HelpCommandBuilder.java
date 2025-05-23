package backend.academy.bot.service.commandBuilder.impl;

import backend.academy.bot.exceptions.UnavailableCommandException;
import backend.academy.bot.exceptions.UnregisteredException;
import backend.academy.bot.model.UserState;
import backend.academy.bot.model.command.Command;
import backend.academy.bot.model.command.impl.HelpCommand;
import backend.academy.bot.repository.StateRepository;
import backend.academy.bot.service.commandBuilder.CommandBuilder;
import backend.academy.bot.util.BotMessages;
import com.pengrad.telegrambot.model.Update;
import org.springframework.stereotype.Component;

@Component
public class HelpCommandBuilder extends CommandBuilder implements BotMessages {

    protected HelpCommandBuilder(StateRepository stateRepository) {
        super(stateRepository);
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
        return new HelpCommand();
    }
}
