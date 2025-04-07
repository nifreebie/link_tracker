package backend.academy.scrapper.model.dto.request;

import java.util.List;

public record ChangeLinkTagsRequest(String url, List<String> tags) {}
