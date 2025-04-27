package backend.academy.bot.service.commandHandler.impl;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.model.command.impl.UntrackCommand;
import backend.academy.bot.repository.StateRepository;
import backend.academy.bot.service.commandHandler.CommandHandler;
import backend.academy.bot.util.RedisCache;
import org.springframework.stereotype.Component;

@Component
public class UntrackCommandHandler extends CommandHandler<UntrackCommand> {

    private final RedisCache redisCache;

    protected UntrackCommandHandler(StateRepository repository, ScrapperClient scrapperClient, RedisCache redisCache) {
        super(repository, scrapperClient);
        this.redisCache = redisCache;
    }

    @Override
    public String handle(UntrackCommand command) {
        String key = "bot:list:" + command.chatId();
        redisCache.invalidate(key);
        return scrapperClient.untrack(command.chatId(), command.url()).block();
    }
}
