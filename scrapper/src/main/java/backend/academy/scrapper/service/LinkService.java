package backend.academy.scrapper.service;

import backend.academy.scrapper.dto.request.AddLinkRequest;
import backend.academy.scrapper.model.Link;
import java.util.List;

public interface LinkService {
    Link follow(AddLinkRequest request, Integer telegramChatId);

    List<Link> getUserLinks(Integer id);

    Link unfollow(String url, Integer id);
}
