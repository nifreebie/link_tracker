package backend.academy.bot.service.commandHandler.impl;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.model.command.impl.CreateTagCommand;
import backend.academy.bot.repository.StateRepository;
import backend.academy.bot.service.commandHandler.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class CreateTagCommandHandler extends CommandHandler<CreateTagCommand> {

    protected CreateTagCommandHandler(StateRepository repository, ScrapperClient scrapperClient) {
        super(repository, scrapperClient);
    }

    @Override
    public String handle(CreateTagCommand command) {
        return scrapperClient.createTag(command.chatId(), command.name()).block();
    }
}
