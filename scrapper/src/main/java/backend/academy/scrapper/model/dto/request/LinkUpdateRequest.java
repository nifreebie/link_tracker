package backend.academy.scrapper.model.dto.request;

import backend.academy.scrapper.model.domain.EventType;
import java.time.LocalDateTime;
import java.util.List;

public record LinkUpdateRequest(
        long id,
        String url,
        String description,
        List<Long> tgChatIds,
        String title,
        String username,
        LocalDateTime date,
        EventType eventType) {}
