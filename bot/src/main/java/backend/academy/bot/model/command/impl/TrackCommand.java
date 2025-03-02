package backend.academy.bot.model.command.impl;

import backend.academy.bot.model.command.Command;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TrackCommand implements Command {
    private Long chatId;
    private String url;
    private List<String> tags;
    private List<String> filters;
}
