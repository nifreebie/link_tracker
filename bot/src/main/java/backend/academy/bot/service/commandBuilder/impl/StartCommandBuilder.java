package backend.academy.bot.service.commandBuilder.impl;

import backend.academy.bot.exceptions.UnavaliableCommandException;
import backend.academy.bot.model.UserState;
import backend.academy.bot.model.command.Command;
import backend.academy.bot.model.command.impl.StartCommand;
import backend.academy.bot.repository.StateRepository;
import backend.academy.bot.service.commandBuilder.CommandBuilder;
import com.pengrad.telegrambot.model.Update;
import org.springframework.stereotype.Component;

@Component
public class StartCommandBuilder extends CommandBuilder {

    protected StartCommandBuilder(StateRepository stateRepository) {
        super(stateRepository);
    }

    @Override
    public Command build(Update update) {
        UserState state = stateRepository.getStateById(update.message().chat().id());
        if (state == UserState.AWAITING_FILTERS
                || state == UserState.AWAITING_TAGS
                || state == UserState.AWAITING_TRACK_URL
                || state == UserState.AWAITING_UNTRACK_URL) {
            throw new UnavaliableCommandException("Сейчас вы не можете использовать эту команду");
        }
        stateRepository.setState(update.message().chat().id(), UserState.DEFAULT);
        return new StartCommand(update.message().chat().id());
    }
}
