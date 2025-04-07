package backend.academy.scrapper.repository;

import java.util.List;

public interface TelegramChatRepository {
    void saveChat(Long tgChatId);

    void removeChat(Long tgChatId);

    Integer countChatId(Long tgChatId);

    List<Long> findAllUsers();
}
