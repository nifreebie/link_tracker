package backend.academy.bot.service.commandHandler.impl;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.model.command.impl.HelpCommand;
import backend.academy.bot.repository.StateRepository;
import backend.academy.bot.service.commandHandler.CommandHandler;
import backend.academy.bot.util.BotMessages;
import org.springframework.stereotype.Component;

@Component
public class HelpCommandHandler extends CommandHandler<HelpCommand> implements BotMessages {

    protected HelpCommandHandler(StateRepository repository, ScrapperClient scrapperClient) {
        super(repository, scrapperClient);
    }

    @Override
    public String handle(HelpCommand command) {
        return HELP_COMMAND.trim();
    }
}
