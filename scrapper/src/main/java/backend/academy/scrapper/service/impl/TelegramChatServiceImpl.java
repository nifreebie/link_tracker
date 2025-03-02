package backend.academy.scrapper.service.impl;

import backend.academy.scrapper.exceptions.IsAlreadyRegisteredException;
import backend.academy.scrapper.exceptions.NotFoundException;
import backend.academy.scrapper.repository.TelegramChatRepository;
import backend.academy.scrapper.service.TelegramChatService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TelegramChatServiceImpl implements TelegramChatService {

    private final TelegramChatRepository telegramChatRepository;

    @Autowired
    public TelegramChatServiceImpl(TelegramChatRepository telegramChatRepository) {
        this.telegramChatRepository = telegramChatRepository;
    }

    @Override
    public void register(Integer id) {
        if (isRegistered(id)) throw new IsAlreadyRegisteredException("Такой чат уже зарегистрирован");
        telegramChatRepository.saveChat(id);
    }

    @Override
    public void delete(Integer id) {
        if (!isRegistered(id)) throw new NotFoundException("Такого чата не существует");
        telegramChatRepository.removeChat(id);
    }

    @Override
    public List<Integer> getChats() {
        return telegramChatRepository.getAll();
    }

    @Override
    public boolean isRegistered(Integer id) {
        return getChats().contains(id);
    }
}
