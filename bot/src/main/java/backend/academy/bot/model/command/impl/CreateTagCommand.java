package backend.academy.bot.model.command.impl;

import backend.academy.bot.model.command.Command;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CreateTagCommand implements Command {
    private Long chatId;
    private String name;
}
