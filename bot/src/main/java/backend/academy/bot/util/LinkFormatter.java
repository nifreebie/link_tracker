package backend.academy.bot.util;

import backend.academy.bot.model.dto.response.LinkResponse;
import backend.academy.bot.model.dto.response.ListLinksResponse;
import lombok.experimental.UtilityClass;

@UtilityClass
public class LinkFormatter {
    public static String formatLinks(ListLinksResponse response) {
        StringBuilder formatted = new StringBuilder();
        formatted.append("Ваши ссылки: ").append("\n");

        for (LinkResponse link : response.links()) {
            formatted.append("Ссылка: ").append(link.url()).append("\n");
            formatted.append("Тэги: ").append(String.join(", ", link.tags())).append("\n");
            formatted
                    .append("Фильтры: ")
                    .append(String.join(", ", link.filters()))
                    .append("\n");
            formatted.append("------------------------------------\n");
        }
        return formatted.toString();
    }
}
