package backend.academy.scrapper;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import backend.academy.scrapper.model.domain.Link;
import backend.academy.scrapper.model.domain.LinkType;
import backend.academy.scrapper.model.domain.Tag;
import backend.academy.scrapper.model.domain.User;
import backend.academy.scrapper.repository.TagRepository;
import backend.academy.scrapper.repository.jpa.TagRepositoryJpa;
import backend.academy.scrapper.util.LiquibaseMigration;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
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
public class TagRepositoryJpaTest {

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private PostgreSQLContainer<?> postgresContainer;

    @PersistenceContext
    private EntityManager entityManager;

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.PostgreSQLDialect");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
    }

    private final Long telegramChatId = 12345L;

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

        User user = new User();
        user.chatId(telegramChatId);
        entityManager.persist(user);
        Link link = new Link();
        link.url("http://example.com");
        link.filters(List.of());
        link.lastUpdatedAt(LocalDateTime.now(ZoneOffset.UTC));
        link.linkType(LinkType.GITHUB);
        link.tags().clear();
        entityManager.persist(link);
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @Transactional
    void testORMImplementation() {
        assertThat(tagRepository).isInstanceOf(TagRepositoryJpa.class);
    }

    @Test
    @Transactional
    void testCreateTag() {
        String tagName = "testTag";
        String createdTag = tagRepository.createTag(tagName, telegramChatId);
        assertThat(createdTag).isEqualTo(tagName);

        Tag persistedTag = entityManager
                .createQuery(
                        "select t from Tag t where t.name = :name and t.telegramChatId.chatId = :chatId", Tag.class)
                .setParameter("name", tagName)
                .setParameter("chatId", telegramChatId)
                .getSingleResult();
        assertThat(persistedTag).isNotNull();
    }

    @Test
    @Transactional
    void testGetUserTags() {
        String tagName = "testTag";
        tagRepository.createTag(tagName, telegramChatId);
        List<String> userTags = tagRepository.getUserTags(telegramChatId);
        assertThat(userTags.contains(tagName)).isEqualTo(true);
    }

    @Test
    @Transactional
    void testIsTagExists() {
        String tagName = "testTag";
        assertThat(tagRepository.isTagExists(tagName)).isFalse();
        tagRepository.createTag(tagName, telegramChatId);
        assertThat(tagRepository.isTagExists(tagName)).isTrue();
    }
}
