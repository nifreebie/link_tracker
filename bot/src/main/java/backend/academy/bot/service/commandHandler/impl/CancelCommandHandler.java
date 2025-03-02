package backend.academy.bot.service.commandHandler.impl;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.model.UserState;
import backend.academy.bot.model.command.impl.CancelCommand;
import backend.academy.bot.repository.StateRepository;
import backend.academy.bot.service.commandHandler.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class CancelCommandHandler extends CommandHandler<CancelCommand> {
    protected CancelCommandHandler(StateRepository repository, ScrapperClient scrapperClient) {
        super(repository, scrapperClient);
    }

    @Override
    public String handle(CancelCommand command) {
        repository.setState(command.chatId(), UserState.DEFAULT);
        return "Команда отменена";
    }
}
