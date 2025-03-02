package backend.academy.bot.service;

import backend.academy.bot.model.command.Command;

public interface CommandManager {
    String executeCommand(Command command);
}
