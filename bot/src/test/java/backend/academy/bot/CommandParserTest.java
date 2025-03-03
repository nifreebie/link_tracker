package backend.academy.bot;

import static org.junit.jupiter.api.Assertions.assertThrows;

import backend.academy.bot.exceptions.NoSuchCommandException;
import backend.academy.bot.service.CommandParser;
import com.pengrad.telegrambot.model.Update;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {"app.telegram-token=token"})
public class CommandParserTest {

    @Autowired
    private CommandParser commandParser;

    @Test
    void testUnknownCommandInput() {
        assertThrows(NoSuchCommandException.class, () -> commandParser.getCommand("/command", new Update()));
    }
}
