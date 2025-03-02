package backend.academy.bot.util;

import backend.academy.bot.model.command.Command;

@FunctionalInterface
public interface Applyer {
    String apply(Command command);
}
