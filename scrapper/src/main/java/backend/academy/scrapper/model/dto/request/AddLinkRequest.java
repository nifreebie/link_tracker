package backend.academy.scrapper.model.dto.request;

import java.util.List;

public record AddLinkRequest(String link, List<String> tags, List<String> filters) {}
