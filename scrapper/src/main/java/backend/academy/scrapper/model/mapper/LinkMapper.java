package backend.academy.scrapper.model.mapper;

import backend.academy.scrapper.model.domain.Link;
import backend.academy.scrapper.model.domain.Tag;
import backend.academy.scrapper.model.domain.User;
import backend.academy.scrapper.model.dto.LinkDTO;
import java.util.List;

public class LinkMapper {
    public static LinkDTO map(Link link) {
        return new LinkDTO(
                link.id(),
                link.url(),
                link.tags().stream().map(Tag::name).toList(),
                link.filters(),
                link.lastUpdatedAt(),
                link.telegramChatIds().stream().map(User::chatId).toList(),
                link.linkType());
    }

    public static List<LinkDTO> map(List<Link> links) {
        return links.stream().map(LinkMapper::map).toList();
    }
}
