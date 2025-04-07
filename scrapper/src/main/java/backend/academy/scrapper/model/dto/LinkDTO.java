package backend.academy.scrapper.model.dto;

import backend.academy.scrapper.model.domain.LinkType;
import backend.academy.scrapper.model.dto.response.LinkResponse;
import java.time.LocalDateTime;
import java.util.List;

public record LinkDTO(
        int id,
        String url,
        List<String> tags,
        List<String> filters,
        LocalDateTime lastUpdatedAt,
        List<Long> telegramChatIds,
        LinkType linkType) {
    public LinkResponse toResponse() {
        return new LinkResponse(id, url, tags, filters);
    }
}
