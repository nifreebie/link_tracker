package backend.academy.bot.service.commandBuilder;

import backend.academy.bot.model.command.Command;
import backend.academy.bot.repository.StateRepository;
import com.pengrad.telegrambot.model.Update;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public abstract class CommandBuilder {

    protected final StateRepository stateRepository;

    @Autowired
    protected CommandBuilder(StateRepository stateRepository) {
        this.stateRepository = stateRepository;
    }

    public abstract Command build(Update update);
}
