package backend.academy.bot.util;

import backend.academy.bot.model.dto.request.LinkUpdateRequest;
import java.time.format.DateTimeFormatter;
import lombok.experimental.UtilityClass;

@UtilityClass
public class UpdateFormatter {
    public static String formatMessage(LinkUpdateRequest request) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDate = request.date().format(formatter);

        String eventTypeDescription =
                switch (request.eventType()) {
                    case COMMIT -> "Коммит";
                    case PR -> "Pull Request";
                    case ISSUE -> "Issue";
                    case ANSWER -> "Ответ";
                    case COMMENT -> "Комментарий";
                    case NO_CHANGES -> null;
                };

        return String.format(
                "🔗 Обновление ссылки: %s%n" + "📌 Заголовок: %s%n"
                        + "📄 Описание: %s%n"
                        + "👤 Автор: %s%n"
                        + "📅 Дата: %s%n"
                        + "⚡ Действие: %s%n",
                request.url(),
                request.title(),
                request.description(),
                request.username(),
                formattedDate,
                eventTypeDescription);
    }
}
