package backend.academy.scrapper.repository.jpa;

import backend.academy.scrapper.model.domain.Link;
import backend.academy.scrapper.model.domain.Tag;
import backend.academy.scrapper.model.domain.User;
import backend.academy.scrapper.model.dto.LinkDTO;
import backend.academy.scrapper.repository.LinkRepository;
import backend.academy.scrapper.repository.TagRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@ConditionalOnProperty(name = "app.access-type", havingValue = "ORM")
@Repository
public class TagRepositoryJpa implements TagRepository {

    @PersistenceContext
    private EntityManager entityManager;

    private final LinkRepository linkRepository;

    @Autowired
    public TagRepositoryJpa(LinkRepository linkRepository) {
        this.linkRepository = linkRepository;
    }

    @Transactional
    @Override
    public String createTag(String name, Long telegramChatId) {
        User user = entityManager
                .createQuery("select u from User u where u.chatId = :chatId", User.class)
                .setParameter("chatId", telegramChatId)
                .getSingleResult();
        Tag tag = new Tag();
        tag.name(name);
        tag.telegramChatId(user);
        entityManager.persist(tag);
        return tag.name();
    }

    @Override
    public List<String> getUserTags(Long telegramChatId) {
        return entityManager
                .createQuery("select t from Tag t where t.telegramChatId.chatId = :chatId", Tag.class)
                .setParameter("chatId", telegramChatId)
                .getResultList()
                .stream()
                .map(Tag::name)
                .toList();
    }

    private Tag findTagByName(String name, Long telegramChatId) {
        return entityManager
                .createQuery(
                        "select t from Tag t where t.name = :name and t.telegramChatId.chatId = :chatId", Tag.class)
                .setParameter("name", name)
                .setParameter("chatId", telegramChatId)
                .getSingleResult();
    }

    @Transactional
    @Override
    public void addLinkTag(String tagName, String url, Long telegramChatId) {
        LinkDTO linkDTO = linkRepository.findLinkByUrl(url);
        Link link = entityManager.find(Link.class, linkDTO.id());
        Tag tag = findTagByName(tagName, telegramChatId);
        link.tags().add(tag);
        entityManager.merge(link);
    }

    @Transactional
    @Override
    public void removeLinkTag(String tagName, String url, Long telegramChatId) {
        LinkDTO linkDTO = linkRepository.findLinkByUrl(url);
        Link link = entityManager.find(Link.class, linkDTO.id());
        Tag tag = findTagByName(tagName, telegramChatId);
        link.tags().remove(tag);
        entityManager.merge(link);
    }

    @Transactional
    @Override
    public boolean isTagExists(String name) {
        int count = entityManager
                .createQuery("select count(*) from Tag t where t.name = :name", Long.class)
                .setParameter("name", name)
                .getSingleResult()
                .intValue();
        return count > 0;
    }
}
