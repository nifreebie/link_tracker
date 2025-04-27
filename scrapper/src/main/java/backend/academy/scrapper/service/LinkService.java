package backend.academy.scrapper.service;

import backend.academy.scrapper.model.dto.LinkDTO;
import backend.academy.scrapper.model.dto.request.AddLinkRequest;
import backend.academy.scrapper.model.dto.response.LinkResponse;
import java.util.List;

public interface LinkService {
    LinkDTO follow(AddLinkRequest request, Long chatId);

    List<LinkResponse> getUserLinks(Long chatId);

    LinkDTO unfollow(String url, Long chatId);

    void addLinkTags(List<String> tagNames, String url, Long chatId);

    void removeLinkTags(List<String> tagName, String url, Long chatId);
}
