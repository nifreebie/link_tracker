package backend.academy.bot;

import static org.junit.jupiter.api.Assertions.assertEquals;

import backend.academy.bot.dto.response.LinkResponse;
import backend.academy.bot.dto.response.ListLinksResponse;
import backend.academy.bot.util.LinkFormatter;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {"app.telegram-token=token"})
public class ListFormatterTest {
    @Test
    void testFormatLinks() {
        ListLinksResponse response = new ListLinksResponse(
                List.of(new LinkResponse(
                        1, "https://example.com/", List.of("tag1", "tag2"), List.of("filter1", "filter2"))),
                1);

        String expectedOutput = "Ваши ссылки: \n" + "Ссылка: https://example.com/\n"
                + "Тэги: tag1, tag2\n"
                + "Фильтры: filter1, filter2\n"
                + "------------------------------------\n";

        assertEquals(expectedOutput, LinkFormatter.formatLinks(response));
    }
}
