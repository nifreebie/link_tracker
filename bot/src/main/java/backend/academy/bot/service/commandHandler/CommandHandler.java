package backend.academy.bot.service.commandHandler;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.model.command.Command;
import backend.academy.bot.repository.StateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public abstract class CommandHandler<T extends Command> {

    protected final StateRepository repository;

    protected final ScrapperClient scrapperClient;

    @Autowired
    protected CommandHandler(StateRepository repository, ScrapperClient scrapperClient) {
        this.repository = repository;
        this.scrapperClient = scrapperClient;
    }

    public abstract String handle(T command);
}
