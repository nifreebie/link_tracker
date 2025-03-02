package backend.academy.bot.dto.request;

import java.util.List;

public record AddLinkRequest(String link, List<String> tags, List<String> filters) {}
