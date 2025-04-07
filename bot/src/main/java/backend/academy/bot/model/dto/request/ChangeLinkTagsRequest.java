package backend.academy.bot.model.dto.request;

import java.util.List;

public record ChangeLinkTagsRequest(String url, List<String> tags) {}
