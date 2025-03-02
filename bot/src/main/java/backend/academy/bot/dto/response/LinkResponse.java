package backend.academy.bot.dto.response;

import java.util.List;

public record LinkResponse(int id, String url, List<String> tags, List<String> filters) {}
