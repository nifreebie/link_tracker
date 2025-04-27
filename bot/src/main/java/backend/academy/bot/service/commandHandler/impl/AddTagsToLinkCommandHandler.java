package backend.academy.bot.service.commandHandler.impl;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.model.command.impl.AddTagsToLinkCommand;
import backend.academy.bot.repository.StateRepository;
import backend.academy.bot.service.commandHandler.CommandHandler;
import backend.academy.bot.util.RedisCache;
import org.springframework.stereotype.Component;

@Component
public class AddTagsToLinkCommandHandler extends CommandHandler<AddTagsToLinkCommand> {

    private final RedisCache redisCache;

    protected AddTagsToLinkCommandHandler(
            StateRepository repository, ScrapperClient scrapperClient, RedisCache redisCache) {
        super(repository, scrapperClient);
        this.redisCache = redisCache;
    }

    @Override
    public String handle(AddTagsToLinkCommand command) {
        String key = "bot:list:" + command.chatId();
        redisCache.invalidate(key);
        return scrapperClient
                .addTags(command.chatId(), command.url(), command.tags())
                .block();
    }
}
