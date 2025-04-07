package backend.academy.scrapper.model.dto.response;

import java.util.List;

public record ListLinksResponse(List<LinkResponse> links, int size) {}
