package backend.academy.bot.model;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommandContext {
    private String url;
    private List<String> tags;
    private List<String> filters;
}
