package backend.academy.scrapper.service.impl;

import backend.academy.scrapper.exceptions.IsAlreadyRegisteredException;
import backend.academy.scrapper.exceptions.NotFoundException;
import backend.academy.scrapper.repository.TelegramChatRepository;
import backend.academy.scrapper.service.TelegramChatService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TelegramChatServiceImpl implements TelegramChatService {

    private final TelegramChatRepository telegramChatRepository;

    @Override
    public void register(Long id) {
        if (isRegistered(id)) throw new IsAlreadyRegisteredException("Такой чат уже зарегистрирован");
        telegramChatRepository.saveChat(id);
    }

    @Override
    public void delete(Long id) {
        if (!isRegistered(id)) throw new NotFoundException("Такого чата не существует");
        telegramChatRepository.removeChat(id);
    }

    @Override
    public boolean isRegistered(Long id) {
        System.out.println(telegramChatRepository.countChatId(id));
        return telegramChatRepository.countChatId(id) > 0;
    }

    @Override
    public List<Long> getAllUsers() {
        List<Long> users = telegramChatRepository.findAllUsers();
        if (users == null) return List.of();
        return users;
    }
}
