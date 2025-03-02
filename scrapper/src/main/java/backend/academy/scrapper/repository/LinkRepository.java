package backend.academy.scrapper.repository;

import backend.academy.scrapper.model.Link;
import java.util.List;

public interface LinkRepository {
    List<Link> findUserLinks(Integer telegramChatId);

    void saveLink(Link link);

    List<Link> getAll();

    boolean isUrlExists(String url);

    Link removeLinkByUrlAndTelegramChatId(String url, Integer telegramChatId);

    Link findLinkByUrl(String url);
}
