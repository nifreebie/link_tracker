package backend.academy.bot.service.commandHandler.impl;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.model.command.impl.RemoveTagsFromLinkCommand;
import backend.academy.bot.repository.StateRepository;
import backend.academy.bot.service.commandHandler.CommandHandler;
import backend.academy.bot.util.RedisCache;
import org.springframework.stereotype.Component;

@Component
public class RemoveTagsFromLinkCommandHandler extends CommandHandler<RemoveTagsFromLinkCommand> {

    private final RedisCache redisCache;

    protected RemoveTagsFromLinkCommandHandler(
            StateRepository repository, ScrapperClient scrapperClient, RedisCache redisCache) {
        super(repository, scrapperClient);
        this.redisCache = redisCache;
    }

    @Override
    public String handle(RemoveTagsFromLinkCommand command) {
        String key = "bot:list:" + command.chatId();
        redisCache.invalidate(key);
        return scrapperClient
                .removeTags(command.chatId(), command.url(), command.tags())
                .block();
    }
}
