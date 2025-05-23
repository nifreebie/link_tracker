package backend.academy.bot.service.commandBuilder.impl;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.exceptions.UnavailableCommandException;
import backend.academy.bot.model.UserState;
import backend.academy.bot.model.command.Command;
import backend.academy.bot.model.command.impl.StartCommand;
import backend.academy.bot.repository.StateRepository;
import backend.academy.bot.service.commandBuilder.CommandBuilder;
import backend.academy.bot.util.BotMessages;
import com.pengrad.telegrambot.model.Update;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class StartCommandBuilder extends CommandBuilder implements BotMessages {

    private final ScrapperClient scrapperClient;

    protected StartCommandBuilder(StateRepository stateRepository, ScrapperClient scrapperClient) {
        super(stateRepository);
        this.scrapperClient = scrapperClient;
    }

    @Override
    public Command build(Update update) {
        Map<Long, UserState> userStates = new HashMap<>();
        List<Long> users = scrapperClient.getAllUsers().block();
        if (users != null) users.forEach(user -> userStates.put(user, UserState.DEFAULT));
        stateRepository.initUserState(userStates);
        UserState state = stateRepository.getStateById(update.message().chat().id());
        if (state != UserState.DEFAULT) {
            throw new UnavailableCommandException(COMMAND_NOT_ALLOWED);
        }
        stateRepository.setState(update.message().chat().id(), UserState.DEFAULT);
        return new StartCommand(update.message().chat().id());
    }
}
