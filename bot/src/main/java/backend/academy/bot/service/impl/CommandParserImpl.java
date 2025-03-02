package backend.academy.bot.service.impl;

import backend.academy.bot.exceptions.NoSuchCommandException;
import backend.academy.bot.model.command.Command;
import backend.academy.bot.service.CommandParser;
import backend.academy.bot.service.commandBuilder.CommandBuilder;
import backend.academy.bot.service.commandBuilder.impl.CancelCommandBuilder;
import backend.academy.bot.service.commandBuilder.impl.HelpCommandBuilder;
import backend.academy.bot.service.commandBuilder.impl.ListCommandBuilder;
import backend.academy.bot.service.commandBuilder.impl.StartCommandBuilder;
import backend.academy.bot.service.commandBuilder.impl.TrackCommandBuilder;
import backend.academy.bot.service.commandBuilder.impl.UntrackCommandBuilder;
import com.pengrad.telegrambot.model.Update;
import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CommandParserImpl implements CommandParser {
    private Map<String, CommandBuilder> commandBuilders;

    private final HelpCommandBuilder helpCommandBuilder;
    private final ListCommandBuilder listCommandBuilder;
    private final StartCommandBuilder startCommandBuilder;
    private final TrackCommandBuilder trackCommandBuilder;
    private final UntrackCommandBuilder untrackCommandBuilder;
    private final CancelCommandBuilder cancelCommandBuilder;

    @Autowired
    public CommandParserImpl(
            HelpCommandBuilder helpCommandBuilder,
            ListCommandBuilder listCommandBuilder,
            StartCommandBuilder startCommandBuilder,
            TrackCommandBuilder trackCommandBuilder,
            UntrackCommandBuilder untrackCommandBuilder,
            CancelCommandBuilder cancelCommandBuilder) {
        this.helpCommandBuilder = helpCommandBuilder;
        this.listCommandBuilder = listCommandBuilder;
        this.startCommandBuilder = startCommandBuilder;
        this.trackCommandBuilder = trackCommandBuilder;
        this.untrackCommandBuilder = untrackCommandBuilder;
        this.cancelCommandBuilder = cancelCommandBuilder;
    }

    @PostConstruct
    public void init() {
        commandBuilders = new HashMap<>();
        commandBuilders.put("/help", helpCommandBuilder);
        commandBuilders.put("/start", startCommandBuilder);
        commandBuilders.put("/list", listCommandBuilder);
        commandBuilders.put("/track", trackCommandBuilder);
        commandBuilders.put("/untrack", untrackCommandBuilder);
        commandBuilders.put("/cancel", cancelCommandBuilder);
    }

    @Override
    public Command getCommand(String str, Update update) {
        if (!commandBuilders.containsKey(str)) {
            throw new NoSuchCommandException("Команды " + str + " не существует");
        }
        return commandBuilders.get(str).build(update);
    }
}
