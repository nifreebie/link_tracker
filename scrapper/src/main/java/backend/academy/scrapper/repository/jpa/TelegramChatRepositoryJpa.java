package backend.academy.scrapper.repository.jpa;

import backend.academy.scrapper.model.domain.User;
import backend.academy.scrapper.repository.TelegramChatRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@ConditionalOnProperty(name = "app.access-type", havingValue = "ORM")
@Repository
public class TelegramChatRepositoryJpa implements TelegramChatRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    @Override
    public void saveChat(Long tgChatId) {
        User user = new User();
        user.chatId(tgChatId);
        entityManager.persist(user);
    }

    @Transactional
    @Override
    public void removeChat(Long tgChatId) {
        entityManager
                .createQuery("delete from User where chatId = :chatId")
                .setParameter("chatId", tgChatId)
                .executeUpdate();
    }

    @Transactional
    @Override
    public Integer countChatId(Long tgChatId) {
        return entityManager
                .createQuery("select count(t) from User t where t.chatId = :chatId", Long.class)
                .setParameter("chatId", tgChatId)
                .getSingleResult()
                .intValue();
    }

    @Transactional
    @Override
    public List<Long> findAllUsers() {
        return entityManager
                .createQuery("select t.chatId from User t ", Long.class)
                .getResultList();
    }
}
