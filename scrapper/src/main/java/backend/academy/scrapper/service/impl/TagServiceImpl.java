package backend.academy.scrapper.service.impl;

import backend.academy.scrapper.exceptions.IsAlreadyRegisteredException;
import backend.academy.scrapper.repository.TagRepository;
import backend.academy.scrapper.service.TagService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepositoryJpa;

    @Override
    public String create(String tagName, Long telegramChatId) {
        if (tagRepositoryJpa.getUserTags(telegramChatId).contains(tagName)) {
            throw new IsAlreadyRegisteredException("У вас уже существует тэг с таким именем");
        }
        return tagRepositoryJpa.createTag(tagName, telegramChatId);
    }

    @Override
    public List<String> getUserTags(Long telegramChatId) {
        return tagRepositoryJpa.getUserTags(telegramChatId);
    }
}
