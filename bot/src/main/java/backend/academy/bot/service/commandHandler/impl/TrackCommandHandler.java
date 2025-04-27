package backend.academy.bot.service.commandHandler.impl;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.model.command.impl.TrackCommand;
import backend.academy.bot.repository.StateRepository;
import backend.academy.bot.service.commandHandler.CommandHandler;
import backend.academy.bot.util.RedisCache;
import org.springframework.stereotype.Component;

@Component
public class TrackCommandHandler extends CommandHandler<TrackCommand> {

    private final RedisCache redisCache;

    protected TrackCommandHandler(StateRepository repository, ScrapperClient scrapperClient, RedisCache redisCache) {
        super(repository, scrapperClient);
        this.redisCache = redisCache;
    }

    @Override
    public String handle(TrackCommand command) {
        String key = "bot:list:" + command.chatId();
        redisCache.invalidate(key);
        return scrapperClient
                .track(command.chatId(), command.url(), command.tags(), command.filters())
                .block();
    }
}
