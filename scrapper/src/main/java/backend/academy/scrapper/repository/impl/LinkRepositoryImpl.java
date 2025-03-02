package backend.academy.scrapper.repository.impl;

import backend.academy.scrapper.model.Link;
import backend.academy.scrapper.repository.LinkRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.stereotype.Repository;

@Repository
public class LinkRepositoryImpl implements LinkRepository {
    private final List<Link> links = Collections.synchronizedList(new ArrayList<>());

    @Override
    public List<Link> findUserLinks(Integer telegramChatId) {
        List<Link> userLinks = new ArrayList<>();
        links.stream()
                .filter(link -> link.telegramChatIds().contains(telegramChatId))
                .forEach(userLinks::add);
        return userLinks;
    }

    @Override
    public void saveLink(Link link) {
        links.add(link);
    }

    @Override
    public List<Link> getAll() {
        return Collections.unmodifiableList(links);
    }

    @Override
    public boolean isUrlExists(String url) {
        boolean exists = false;
        for (Link link : links) {
            if (link.url().equals(url)) {
                exists = true;
                break;
            }
        }
        return exists;
    }

    @Override
    public Link removeLinkByUrlAndTelegramChatId(String url, Integer telegramChatId) {
        Link removedLink = null;
        List<Link> userLinks = findUserLinks(telegramChatId);
        for (Link link : userLinks) {
            if (link.url().equals(url)) {
                removedLink = link;
                links.remove(link);
                break;
            }
        }
        return removedLink;
    }

    @Override
    public Link findLinkByUrl(String url) {
        AtomicReference<Link> returnedLink = new AtomicReference<>();
        links.forEach(link -> {
            if (link.url().equals(url)) returnedLink.set(link);
        });
        return returnedLink.get();
    }
}
