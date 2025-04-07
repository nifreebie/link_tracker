package backend.academy.bot.model.dto.response;

import java.util.List;

public record LinkResponse(int id, String url, List<String> tags, List<String> filters) {}
