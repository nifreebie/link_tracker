package backend.academy.bot.service.impl;

import backend.academy.bot.model.command.Command;
import backend.academy.bot.model.command.impl.AddTagsToLinkCommand;
import backend.academy.bot.model.command.impl.CancelCommand;
import backend.academy.bot.model.command.impl.CreateTagCommand;
import backend.academy.bot.model.command.impl.HelpCommand;
import backend.academy.bot.model.command.impl.ListCommand;
import backend.academy.bot.model.command.impl.RemoveTagsFromLinkCommand;
import backend.academy.bot.model.command.impl.StartCommand;
import backend.academy.bot.model.command.impl.TrackCommand;
import backend.academy.bot.model.command.impl.UntrackCommand;
import backend.academy.bot.service.CommandManager;
import backend.academy.bot.service.commandHandler.impl.AddTagsToLinkCommandHandler;
import backend.academy.bot.service.commandHandler.impl.CancelCommandHandler;
import backend.academy.bot.service.commandHandler.impl.CreateTagCommandHandler;
import backend.academy.bot.service.commandHandler.impl.HelpCommandHandler;
import backend.academy.bot.service.commandHandler.impl.ListCommandHandler;
import backend.academy.bot.service.commandHandler.impl.RemoveTagsFromLinkCommandHandler;
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
    private final CreateTagCommandHandler createTagCommandHandler;
    private final AddTagsToLinkCommandHandler addTagsToLinkCommandHandler;
    private final RemoveTagsFromLinkCommandHandler removeTagsFromLinkCommandHandler;

    @Autowired
    public CommandManagerImpl(
            HelpCommandHandler helpCommandHandler,
            ListCommandHandler listCommandHandler,
            StartCommandHandler startCommandHandler,
            TrackCommandHandler trackCommandHandler,
            UntrackCommandHandler untrackCommandHandler,
            CancelCommandHandler cancelCommandHandler,
            CreateTagCommandHandler createTagCommandHandler,
            AddTagsToLinkCommandHandler addTagsToLinkCommandHandler,
            RemoveTagsFromLinkCommandHandler removeTagsFromLinkCommandHandler) {
        this.helpCommandHandler = helpCommandHandler;
        this.listCommandHandler = listCommandHandler;
        this.startCommandHandler = startCommandHandler;
        this.trackCommandHandler = trackCommandHandler;
        this.untrackCommandHandler = untrackCommandHandler;
        this.cancelCommandHandler = cancelCommandHandler;
        this.createTagCommandHandler = createTagCommandHandler;
        this.addTagsToLinkCommandHandler = addTagsToLinkCommandHandler;
        this.removeTagsFromLinkCommandHandler = removeTagsFromLinkCommandHandler;
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
        commandHandlers.put(
                CreateTagCommand.class, command -> createTagCommandHandler.handle((CreateTagCommand) command));
        commandHandlers.put(
                AddTagsToLinkCommand.class,
                command -> addTagsToLinkCommandHandler.handle((AddTagsToLinkCommand) command));
        commandHandlers.put(
                RemoveTagsFromLinkCommand.class,
                command -> removeTagsFromLinkCommandHandler.handle((RemoveTagsFromLinkCommand) command));
    }

    @Override
    public String executeCommand(Command command) {
        return commandHandlers.get(command.getClass()).apply(command);
    }
}
