package backend.academy.scrapper.service;

import backend.academy.scrapper.model.dto.LinkDTO;
import backend.academy.scrapper.model.dto.request.AddLinkRequest;
import java.util.List;

public interface LinkService {
    LinkDTO follow(AddLinkRequest request, Long chatId);

    List<LinkDTO> getUserLinks(Long chatId);

    LinkDTO unfollow(String url, Long chatId);

    void addLinkTag(String tagName, String url, Long chatId);

    void removeLinkTag(String tagName, String url, Long chatId);
}
