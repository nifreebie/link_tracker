package backend.academy.bot.service.commandHandler.impl;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.model.command.impl.StartCommand;
import backend.academy.bot.repository.StateRepository;
import backend.academy.bot.service.commandHandler.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class StartCommandHandler extends CommandHandler<StartCommand> {

    protected StartCommandHandler(StateRepository repository, ScrapperClient scrapperClient) {
        super(repository, scrapperClient);
    }

    @Override
    public String handle(StartCommand command) {
        return scrapperClient.register(command.chatId()).block();
    }
}
