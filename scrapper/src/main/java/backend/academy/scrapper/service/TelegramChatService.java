package backend.academy.scrapper.service;

import java.util.List;

public interface TelegramChatService {
    void register(Long id);

    void delete(Long id);

    boolean isRegistered(Long id);

    List<Long> getAllUsers();
}
