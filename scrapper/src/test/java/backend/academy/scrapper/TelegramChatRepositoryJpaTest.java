package backend.academy.scrapper;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import backend.academy.scrapper.repository.TelegramChatRepository;
import backend.academy.scrapper.repository.jpa.TelegramChatRepositoryJpa;
import backend.academy.scrapper.util.LiquibaseMigration;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest
@Import({TestcontainersConfiguration.class})
@TestPropertySource(properties = "app.access-type=ORM")
public class TelegramChatRepositoryJpaTest {
    @Autowired
    private TelegramChatRepository repository;

    @Autowired
    private PostgreSQLContainer<?> postgresContainer;

    @PersistenceContext
    private EntityManager entityManager;

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.PostgreSQLDialect");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
    }

    @BeforeEach
    @Transactional
    void setUp() {
        LiquibaseMigration.migrate(
                Paths.get("../migrations/master.xml"),
                postgresContainer.getUsername(),
                postgresContainer.getPassword(),
                postgresContainer.getJdbcUrl());
        entityManager
                .createNativeQuery("TRUNCATE TABLE link_tag, user_link, tags, links, users CASCADE")
                .executeUpdate();
        repository.findAllUsers().forEach(chatId -> repository.removeChat(chatId));
    }

    @Test
    @Transactional
    void testORMImplementation() {
        assertThat(repository).isInstanceOf(TelegramChatRepositoryJpa.class);
    }

    @Test
    @Transactional
    void testSaveChat() {
        Long chatId = 12345L;
        repository.saveChat(chatId);
        Integer count = repository.countChatId(chatId);
        assertThat(count).isEqualTo(1);
    }

    @Test
    @Transactional
    public void testRemoveChat() {
        Long chatId = 12345L;
        repository.saveChat(chatId);
        repository.removeChat(chatId);
        Integer count = repository.countChatId(chatId);
        assertThat(count).isZero();
    }

    @Test
    @Transactional
    public void testFindAllUsers() {
        repository.saveChat(11111L);
        repository.saveChat(22222L);
        List<Long> chatIds = repository.findAllUsers();
        assertThat(chatIds.size()).isEqualTo(2);
    }
}
