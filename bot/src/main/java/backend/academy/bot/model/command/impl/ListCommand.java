package backend.academy.bot.model.command.impl;

import backend.academy.bot.model.command.Command;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ListCommand implements Command {
    private Long chatId;
}
