package backend.academy.scrapper.model.dto;

import java.util.List;

public record TagDTO(int id, String name, long telegramChatId, List<String> links) {}
