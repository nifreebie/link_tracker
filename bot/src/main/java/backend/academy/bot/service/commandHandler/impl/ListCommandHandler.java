package backend.academy.bot.service.commandHandler.impl;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.model.command.impl.ListCommand;
import backend.academy.bot.repository.StateRepository;
import backend.academy.bot.service.commandHandler.CommandHandler;
import backend.academy.bot.util.LinkFormatter;
import org.springframework.stereotype.Component;

@Component
public class ListCommandHandler extends CommandHandler<ListCommand> {
    protected ListCommandHandler(StateRepository repository, ScrapperClient scrapperClient) {
        super(repository, scrapperClient);
    }

    @Override
    public String handle(ListCommand command) {
        return scrapperClient
                .getUserLinks(command.chatId())
                .map(response -> {
                    if (response.size() == 0) return "У вас нет отслеживаемых ссылок";
                    else return LinkFormatter.formatLinks(response);
                })
                .block();
    }
}
