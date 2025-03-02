package backend.academy.bot.service.impl;

import backend.academy.bot.model.command.Command;
import backend.academy.bot.model.command.impl.CancelCommand;
import backend.academy.bot.model.command.impl.HelpCommand;
import backend.academy.bot.model.command.impl.ListCommand;
import backend.academy.bot.model.command.impl.StartCommand;
import backend.academy.bot.model.command.impl.TrackCommand;
import backend.academy.bot.model.command.impl.UntrackCommand;
import backend.academy.bot.service.CommandManager;
import backend.academy.bot.service.commandHandler.impl.CancelCommandHandler;
import backend.academy.bot.service.commandHandler.impl.HelpCommandHandler;
import backend.academy.bot.service.commandHandler.impl.ListCommandHandler;
import backend.academy.bot.service.commandHandler.impl.StartCommandHandler;
import backend.academy.bot.service.commandHandler.impl.TrackCommandHandler;
import backend.academy.bot.service.commandHandler.impl.UntrackCommandHandler;
import backend.academy.bot.util.Applyer;
import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CommandManagerImpl implements CommandManager {
    private Map<Class<? extends Command>, Applyer> commandHandlers;

    private final HelpCommandHandler helpCommandHandler;
    private final ListCommandHandler listCommandHandler;
    private final StartCommandHandler startCommandHandler;
    private final TrackCommandHandler trackCommandHandler;
    private final UntrackCommandHandler untrackCommandHandler;
    private final CancelCommandHandler cancelCommandHandler;

    @Autowired
    public CommandManagerImpl(
            HelpCommandHandler helpCommandHandler,
            ListCommandHandler listCommandHandler,
            StartCommandHandler startCommandHandler,
            TrackCommandHandler trackCommandHandler,
            UntrackCommandHandler untrackCommandHandler,
            CancelCommandHandler cancelCommandHandler) {
        this.helpCommandHandler = helpCommandHandler;
        this.listCommandHandler = listCommandHandler;
        this.startCommandHandler = startCommandHandler;
        this.trackCommandHandler = trackCommandHandler;
        this.untrackCommandHandler = untrackCommandHandler;
        this.cancelCommandHandler = cancelCommandHandler;
    }

    @PostConstruct
    public void init() {
        commandHandlers = new HashMap<>();
        commandHandlers.put(HelpCommand.class, command -> helpCommandHandler.handle((HelpCommand) command));
        commandHandlers.put(StartCommand.class, command -> startCommandHandler.handle((StartCommand) command));
        commandHandlers.put(ListCommand.class, command -> listCommandHandler.handle((ListCommand) command));
        commandHandlers.put(TrackCommand.class, command -> trackCommandHandler.handle((TrackCommand) command));
        commandHandlers.put(UntrackCommand.class, command -> untrackCommandHandler.handle((UntrackCommand) command));
        commandHandlers.put(CancelCommand.class, command -> cancelCommandHandler.handle((CancelCommand) command));
    }

    @Override
    public String executeCommand(Command command) {
        return commandHandlers.get(command.getClass()).apply(command);
    }
}
