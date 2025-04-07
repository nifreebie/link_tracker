package backend.academy.scrapper.model.dto.response;

import java.util.List;

public record LinkResponse(long id, String url, List<String> tags, List<String> filters) {}
