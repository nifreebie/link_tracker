package backend.academy.bot.service.commandHandler.impl;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.model.command.impl.ListCommand;
import backend.academy.bot.repository.StateRepository;
import backend.academy.bot.service.commandHandler.CommandHandler;
import backend.academy.bot.util.BotMessages;
import backend.academy.bot.util.LinkFormatter;
import backend.academy.bot.util.RedisCache;
import org.springframework.stereotype.Component;

@Component
public class ListCommandHandler extends CommandHandler<ListCommand> implements BotMessages {

    private final RedisCache redisCache;

    protected ListCommandHandler(StateRepository repository, ScrapperClient scrapperClient, RedisCache redisCache) {
        super(repository, scrapperClient);
        this.redisCache = redisCache;
    }

    @Override
    public String handle(ListCommand command) {
        String key = "bot:list:" + command.chatId();
        String cachedResult = redisCache.get(key);
        if (cachedResult != null) {
            return cachedResult;
        }
        String result = scrapperClient
                .getUserLinks(command.chatId())
                .map(response -> {
                    if (response.size() == 0) return EMPTY_LINK_LIST;
                    else return LinkFormatter.formatLinks(response);
                })
                .block();

        if (result != null) {
            redisCache.set(key, result);
        }

        return result;
    }
}
