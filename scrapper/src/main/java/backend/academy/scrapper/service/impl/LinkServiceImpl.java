package backend.academy.scrapper.service.impl;

import backend.academy.scrapper.dto.request.AddLinkRequest;
import backend.academy.scrapper.exceptions.IsAlreadyRegisteredException;
import backend.academy.scrapper.exceptions.NotFoundException;
import backend.academy.scrapper.model.Link;
import backend.academy.scrapper.repository.LinkRepository;
import backend.academy.scrapper.service.LinkService;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LinkServiceImpl implements LinkService {
    private final LinkRepository linkRepository;

    @Autowired
    public LinkServiceImpl(LinkRepository linkRepository) {
        this.linkRepository = linkRepository;
    }

    @Override
    public Link follow(AddLinkRequest request, Integer telegramChatId) {
        if (linkRepository.isUrlExists(request.link())) {
            if (linkRepository.findLinkByUrl(request.link()).telegramChatIds().contains(telegramChatId)) {
                throw new IsAlreadyRegisteredException("Ссылка уже отслеживается");
            } else {
                linkRepository.findLinkByUrl(request.link()).telegramChatIds().add(telegramChatId);
                return linkRepository.findLinkByUrl(request.link());
            }
        } else {
            Link link = new Link(request.link(), request.tags(), request.filters(), telegramChatId);
            linkRepository.saveLink(link);
            return link;
        }
    }

    @Override
    public List<Link> getUserLinks(Integer id) {
        return linkRepository.findUserLinks(id);
    }

    @Override
    public Link unfollow(String url, Integer id) {
        return Optional.ofNullable(linkRepository.removeLinkByUrlAndTelegramChatId(url, id))
            .orElseThrow(() -> new NotFoundException("Ссылка не найдена"));
    }
}
