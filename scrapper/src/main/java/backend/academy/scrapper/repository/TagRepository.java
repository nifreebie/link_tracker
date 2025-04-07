package backend.academy.scrapper.repository;

import java.util.List;

public interface TagRepository {
    String createTag(String name, Long telegramChatId);

    List<String> getUserTags(Long telegramChatId);

    void addLinkTag(String tagName, String url, Long telegramChatId);

    void removeLinkTag(String tagName, String url, Long telegramChatId);

    boolean isTagExists(String name);
}
