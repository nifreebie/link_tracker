package backend.academy.bot;

import static org.junit.jupiter.api.Assertions.assertEquals;

import backend.academy.bot.model.EventType;
import backend.academy.bot.model.dto.request.LinkUpdateRequest;
import backend.academy.bot.util.UpdateFormatter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {"app.telegram-token=token"})
public class UpdateFormatterTest {

    @Test
    void testFormatMessageCommit() {
        LocalDateTime date = LocalDateTime.of(2025, 4, 5, 12, 30, 45);
        LinkUpdateRequest request = new LinkUpdateRequest(
                1,
                "http://example.com",
                "Commit Description",
                List.of(1L),
                "Commit Title",
                "User1",
                date,
                EventType.COMMIT);

        String formattedMessage = UpdateFormatter.formatMessage(request);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String expectedDate = date.format(formatter);

        String expected = String.format(
                "🔗 Обновление ссылки: %s%n" + "📌 Заголовок: %s%n"
                        + "📄 Описание: %s%n"
                        + "👤 Автор: %s%n"
                        + "📅 Дата: %s%n"
                        + "⚡ Действие: %s%n",
                "http://example.com", "Commit Title", "Commit Description", "User1", expectedDate, "Коммит");
        assertEquals(expected, formattedMessage);
    }

    @Test
    void testFormatMessagePR() {
        LocalDateTime date = LocalDateTime.of(2025, 4, 5, 12, 30, 45);
        LinkUpdateRequest request = new LinkUpdateRequest(
                1, "http://example.com", "PR Description", List.of(1L), "PR Title", "User2", date, EventType.PR);

        String formattedMessage = UpdateFormatter.formatMessage(request);
        String expectedDate = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        String expected = String.format(
                "🔗 Обновление ссылки: %s%n" + "📌 Заголовок: %s%n"
                        + "📄 Описание: %s%n"
                        + "👤 Автор: %s%n"
                        + "📅 Дата: %s%n"
                        + "⚡ Действие: %s%n",
                "http://example.com", "PR Title", "PR Description", "User2", expectedDate, "Pull Request");
        assertEquals(expected, formattedMessage);
    }

    @Test
    void testFormatMessageIssue() {
        LocalDateTime date = LocalDateTime.of(2025, 4, 5, 12, 30, 45);
        LinkUpdateRequest request = new LinkUpdateRequest(
                1,
                "http://example.com",
                "Issue Description",
                List.of(1L),
                "Issue Title",
                "User3",
                date,
                EventType.ISSUE);

        String formattedMessage = UpdateFormatter.formatMessage(request);
        String expectedDate = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        String expected = String.format(
                "🔗 Обновление ссылки: %s%n" + "📌 Заголовок: %s%n"
                        + "📄 Описание: %s%n"
                        + "👤 Автор: %s%n"
                        + "📅 Дата: %s%n"
                        + "⚡ Действие: %s%n",
                "http://example.com", "Issue Title", "Issue Description", "User3", expectedDate, "Issue");
        assertEquals(expected, formattedMessage);
    }
}
