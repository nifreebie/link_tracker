package backend.academy.scrapper.service.impl;

import backend.academy.scrapper.exceptions.IsAlreadyRegisteredException;
import backend.academy.scrapper.exceptions.NotFoundException;
import backend.academy.scrapper.model.dto.LinkDTO;
import backend.academy.scrapper.model.dto.request.AddLinkRequest;
import backend.academy.scrapper.model.dto.response.LinkResponse;
import backend.academy.scrapper.repository.LinkRepository;
import backend.academy.scrapper.repository.TagRepository;
import backend.academy.scrapper.service.LinkService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LinkServiceImpl implements LinkService {
    private final LinkRepository linkRepository;
    private final TagRepository tagRepository;

    @Autowired
    public LinkServiceImpl(LinkRepository linkRepository, TagRepository tagRepository) {
        this.linkRepository = linkRepository;
        this.tagRepository = tagRepository;
    }

    @Override
    public LinkDTO follow(AddLinkRequest request, Long telegramChatId) {
        if (linkRepository.isUrlExists(request.link())) {
            if (linkRepository.findLinkByUrl(request.link()).telegramChatIds().contains(telegramChatId))
                throw new IsAlreadyRegisteredException("Ссылка уже отслеживается");
        }
        return linkRepository.saveLink(request.link(), request.tags(), request.filters(), telegramChatId);
    }

    @Override
    public List<LinkResponse> getUserLinks(Long id) {
        List<LinkDTO> links = linkRepository.findUserLinks(id);
        if (links == null) return List.of();

        List<String> tags = tagRepository.getUserTags(id);

        return links.stream()
                .map(link -> {
                    List<String> mutableTags = new ArrayList<>(link.tags());
                    if (tags == null) {
                        mutableTags.clear();
                    } else {
                        mutableTags.removeIf(tag -> !tags.contains(tag));
                    }
                    return new LinkResponse(link.id(), link.url(), mutableTags, link.filters());
                })
                .toList();
    }

    @Override
    public LinkDTO unfollow(String url, Long id) {
        return Optional.ofNullable(linkRepository.removeLinkByUrlAndTelegramChatId(url, id))
                .orElseThrow(() -> new NotFoundException("Ссылка не найдена"));
    }

    @Override
    public void addLinkTags(List<String> tagNames, String url, Long chatId) {
        tagNames.forEach(link -> addLinkTag(link, url, chatId));
    }

    @Override
    public void removeLinkTags(List<String> tagNames, String url, Long chatId) {
        tagNames.forEach(link -> removeLinkTag(link, url, chatId));
    }

    private void addLinkTag(String tagName, String url, Long telegramChatId) {
        if (!linkRepository.isUrlExists(url)) throw new NotFoundException("Такой ссылки не существует");
        if (!tagRepository.isTagExists(tagName)) throw new NotFoundException("Такого тэга не существует");

        LinkDTO link = linkRepository.findLinkByUrl(url);

        if (!linkRepository.findUserLinks(telegramChatId).contains(link))
            throw new NotFoundException("Вы не отслеживаете такую ссылку");

        List<String> tags = tagRepository.getUserTags(telegramChatId);

        List<String> mutableTags = new ArrayList<>(link.tags());

        mutableTags.removeIf(tag -> !tags.contains(tag));

        if (mutableTags.contains(tagName))
            throw new IsAlreadyRegisteredException("Тэг " + tagName + " уже принадлежит этой ссылке");

        tagRepository.addLinkTag(tagName, url, telegramChatId);
    }

    private void removeLinkTag(String tagName, String url, Long telegramChatId) {
        if (!linkRepository.isUrlExists(url)) throw new NotFoundException("Такой ссылки не существует");
        if (!tagRepository.isTagExists(tagName)) throw new NotFoundException("Такого тэга не существует");
        LinkDTO link = linkRepository.findLinkByUrl(url);
        if (!linkRepository.findUserLinks(telegramChatId).contains(link))
            throw new NotFoundException("Вы не отслеживаете такую ссылку");
        List<String> tags = tagRepository.getUserTags(telegramChatId);

        List<String> mutableTags = new ArrayList<>(link.tags());

        mutableTags.removeIf(tag -> !tags.contains(tag));
        if (!mutableTags.contains(tagName))
            throw new NotFoundException("Тэг " + tagName + " не принадлежит этой ссылке");
        tagRepository.removeLinkTag(tagName, url, telegramChatId);
    }
}
