package backend.academy.bot.dto.request;

import java.util.List;

public record LinkUpdateRequest(long id, String url, String description, List<Integer> tgChatIds) {}
