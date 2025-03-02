package backend.academy.scrapper.repository.impl;

import backend.academy.scrapper.repository.TelegramChatRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class TelegramChatRepositoryImpl implements TelegramChatRepository {
    private final List<Integer> chatIds = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void saveChat(Integer tgChatId) {
        chatIds.add(tgChatId);
    }

    @Override
    public void removeChat(Integer tgChatId) {
        chatIds.remove(tgChatId);
    }

    @Override
    public List<Integer> getAll() {
        return Collections.unmodifiableList(chatIds);
    }
}
