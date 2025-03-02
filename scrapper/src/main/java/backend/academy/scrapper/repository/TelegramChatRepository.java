package backend.academy.scrapper.repository;

import java.util.List;

public interface TelegramChatRepository {
    void saveChat(Integer tgChatId);

    void removeChat(Integer tgChatId);

    List<Integer> getAll();
}
