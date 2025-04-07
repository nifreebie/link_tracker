package backend.academy.bot.service.commandHandler.impl;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.model.command.impl.AddTagsToLinkCommand;
import backend.academy.bot.repository.StateRepository;
import backend.academy.bot.service.commandHandler.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class AddTagsToLinkCommandHandler extends CommandHandler<AddTagsToLinkCommand> {

    protected AddTagsToLinkCommandHandler(StateRepository repository, ScrapperClient scrapperClient) {
        super(repository, scrapperClient);
    }

    @Override
    public String handle(AddTagsToLinkCommand command) {
        return scrapperClient
                .addTags(command.chatId(), command.url(), command.tags())
                .block();
    }
}
