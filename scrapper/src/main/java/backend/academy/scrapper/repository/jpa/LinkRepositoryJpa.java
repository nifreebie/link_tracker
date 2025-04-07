package backend.academy.scrapper.repository.jpa;

import backend.academy.scrapper.model.domain.Link;
import backend.academy.scrapper.model.domain.LinkType;
import backend.academy.scrapper.model.domain.Tag;
import backend.academy.scrapper.model.domain.User;
import backend.academy.scrapper.model.dto.LinkDTO;
import backend.academy.scrapper.model.mapper.LinkMapper;
import backend.academy.scrapper.repository.LinkRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@ConditionalOnProperty(name = "app.access-type", havingValue = "ORM")
@Repository
public class LinkRepositoryJpa implements LinkRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    @Override
    public List<LinkDTO> findUserLinks(Long telegramChatId) {
        return entityManager
                .createQuery(
                        "SELECT l FROM Link l " + "WHERE EXISTS ("
                                + "    SELECT 1 FROM l.telegramChatIds u WHERE u.chatId = :chatId"
                                + ")",
                        Link.class)
                .setParameter("chatId", telegramChatId)
                .getResultList()
                .stream()
                .map(LinkMapper::map)
                .toList();
    }

    @Transactional
    @Override
    public LinkDTO saveLink(String url, List<String> tags, List<String> filters, Long telegramChatId) {
        User user = entityManager
                .createQuery("select u from User u where u.chatId = :chatId", User.class)
                .setParameter("chatId", telegramChatId)
                .getSingleResult();
        List<Tag> tagEntities = new ArrayList<>();
        tags.forEach(tag -> tagEntities.add(entityManager
                .createQuery("select t from Tag t where t.name = :tag", Tag.class)
                .setParameter("tag", tag)
                .getSingleResult()));
        Link link;
        try {
            link = entityManager
                    .createQuery("select l from Link l where l.url = :url", Link.class)
                    .setParameter("url", url)
                    .getSingleResult();
            link.telegramChatIds().add(user);
            link.tags().addAll(tagEntities);
            entityManager.merge(link);
        } catch (NoResultException e) {
            link = new Link();
            link.url(url);
            link.filters(filters);
            link.lastUpdatedAt(LocalDateTime.now(ZoneOffset.UTC));
            link.linkType(LinkType.getLinkType(url));
            link.telegramChatIds().add(user);
            link.tags().addAll(tagEntities);
            entityManager.persist(link);
        }
        return LinkMapper.map(link);
    }

    @Transactional
    @Override
    public List<LinkDTO> getAll() {
        return entityManager
                .createQuery("select l from Link l left join fetch l.telegramChatIds", Link.class)
                .getResultList()
                .stream()
                .map(LinkMapper::map)
                .toList();
    }

    @Transactional
    @Override
    public boolean isUrlExists(String url) {
        int count = entityManager
                .createQuery("select count(*) from Link l where l.url = :url", Long.class)
                .setParameter("url", url)
                .getSingleResult()
                .intValue();
        return count > 0;
    }

    @Transactional
    @Override
    public LinkDTO removeLinkByUrlAndTelegramChatId(String url, Long telegramChatId) {
        Link link;
        try {
            link = entityManager
                    .createQuery("select l from Link l where l.url = :url", Link.class)
                    .setParameter("url", url)
                    .getSingleResult();
            User user = entityManager
                    .createQuery("select u from User u where u.chatId = :chatId", User.class)
                    .setParameter("chatId", telegramChatId)
                    .getSingleResult();
            link.telegramChatIds().remove(user);
            entityManager.merge(link);
        } catch (NoResultException e) {
            return null;
        }
        return LinkMapper.map(link);
    }

    @Transactional
    @Override
    public LinkDTO findLinkByUrl(String url) {
        return LinkMapper.map(entityManager
                .createQuery("select l from Link l left join fetch l.telegramChatIds where l.url = :url", Link.class)
                .setParameter("url", url)
                .getSingleResult());
    }

    @Transactional
    @Override
    public void updateLastUpdatedAt(Integer id, LocalDateTime lastUpdatedAt) {
        Link link = entityManager.find(Link.class, id);
        link.lastUpdatedAt(lastUpdatedAt);
        entityManager.merge(link);
    }

    @Override
    @Transactional
    public List<LinkDTO> getPaginatedLinks(Integer offset, Integer limit) {
        return entityManager
                .createQuery("select l from Link l", Link.class)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList()
                .stream()
                .map(LinkMapper::map)
                .toList();
    }
}
