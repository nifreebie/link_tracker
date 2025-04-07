package backend.academy.scrapper.service;

import java.util.List;

public interface TagService {
    String create(String tagName, Long telegramChatId);

    List<String> getUserTags(Long telegramChatId);
}
