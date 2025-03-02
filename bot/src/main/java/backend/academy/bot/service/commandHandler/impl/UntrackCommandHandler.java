package backend.academy.bot.service.commandHandler.impl;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.model.command.impl.UntrackCommand;
import backend.academy.bot.repository.StateRepository;
import backend.academy.bot.service.commandHandler.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class UntrackCommandHandler extends CommandHandler<UntrackCommand> {
    protected UntrackCommandHandler(StateRepository repository, ScrapperClient scrapperClient) {
        super(repository, scrapperClient);
    }

    @Override
    public String handle(UntrackCommand command) {
        return scrapperClient.untrack(command.chatId(), command.url()).block();
    }
}
