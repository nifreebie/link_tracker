package backend.academy.bot.service.commandHandler.impl;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.model.command.impl.HelpCommand;
import backend.academy.bot.repository.StateRepository;
import backend.academy.bot.service.commandHandler.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class HelpCommandHandler extends CommandHandler<HelpCommand> {

    protected HelpCommandHandler(StateRepository repository, ScrapperClient scrapperClient) {
        super(repository, scrapperClient);
    }

    @Override
    public String handle(HelpCommand command) {
        return """
            /start - регистрация пользователя.
            /help - вывод списка доступных команд.
            /track - начать отслеживание ссылки.
            /untrack - прекратить отслеживание ссылки.
            /list - показать список отслеживаемых ссылок (cписок ссылок, полученных при /track)
            /cancel - отменить выполнение команды.
            """
                .trim();
    }
}
