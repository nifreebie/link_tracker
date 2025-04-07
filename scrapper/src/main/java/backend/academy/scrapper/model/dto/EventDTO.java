package backend.academy.scrapper.model.dto;

import backend.academy.scrapper.model.domain.EventType;
import java.time.LocalDateTime;

public record EventDTO(String title, String username, LocalDateTime date, String description, EventType eventType) {}
