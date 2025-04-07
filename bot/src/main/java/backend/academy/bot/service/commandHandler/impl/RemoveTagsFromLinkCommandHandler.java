package backend.academy.bot.service.commandHandler.impl;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.model.command.impl.RemoveTagsFromLinkCommand;
import backend.academy.bot.repository.StateRepository;
import backend.academy.bot.service.commandHandler.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class RemoveTagsFromLinkCommandHandler extends CommandHandler<RemoveTagsFromLinkCommand> {
    protected RemoveTagsFromLinkCommandHandler(StateRepository repository, ScrapperClient scrapperClient) {
        super(repository, scrapperClient);
    }

    @Override
    public String handle(RemoveTagsFromLinkCommand command) {
        return scrapperClient
                .removeTags(command.chatId(), command.url(), command.tags())
                .block();
    }
}
