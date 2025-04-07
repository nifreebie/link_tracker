package backend.academy.scrapper.repository;

import backend.academy.scrapper.model.dto.LinkDTO;
import java.time.LocalDateTime;
import java.util.List;

public interface LinkRepository {
    List<LinkDTO> findUserLinks(Long telegramChatId);

    LinkDTO saveLink(String url, List<String> tags, List<String> filters, Long telegramChatId);

    List<LinkDTO> getAll();

    boolean isUrlExists(String url);

    LinkDTO removeLinkByUrlAndTelegramChatId(String url, Long telegramChatId);

    LinkDTO findLinkByUrl(String url);

    void updateLastUpdatedAt(Integer id, LocalDateTime lastUpdatedAt);

    List<LinkDTO> getPaginatedLinks(Integer offset, Integer limit);
}
