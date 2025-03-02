package backend.academy.scrapper.service;

import java.util.List;

public interface TelegramChatService {
    void register(Integer id);

    void delete(Integer id);

    List<Integer> getChats();

    boolean isRegistered(Integer id);
}
