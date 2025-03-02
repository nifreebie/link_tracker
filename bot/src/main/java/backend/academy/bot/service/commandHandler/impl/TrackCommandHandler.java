package backend.academy.bot.service.commandHandler.impl;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.model.command.impl.TrackCommand;
import backend.academy.bot.repository.StateRepository;
import backend.academy.bot.service.commandHandler.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class TrackCommandHandler extends CommandHandler<TrackCommand> {

    protected TrackCommandHandler(StateRepository repository, ScrapperClient scrapperClient) {
        super(repository, scrapperClient);
    }

    @Override
    public String handle(TrackCommand command) {
        return scrapperClient
                .track(command.chatId(), command.url(), command.tags(), command.filters())
                .block();
    }
}
