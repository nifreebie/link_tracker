package backend.academy.bot.service;

import backend.academy.bot.model.command.Command;
import com.pengrad.telegrambot.model.Update;

public interface CommandParser {
    Command getCommand(String str, Update update);
}
