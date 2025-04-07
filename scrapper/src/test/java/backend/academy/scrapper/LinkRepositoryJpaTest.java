package backend.academy.scrapper;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import backend.academy.scrapper.model.domain.Link;
import backend.academy.scrapper.model.domain.LinkType;
import backend.academy.scrapper.model.domain.Tag;
import backend.academy.scrapper.model.domain.User;
import backend.academy.scrapper.model.dto.LinkDTO;
import backend.academy.scrapper.repository.LinkRepository;
import backend.academy.scrapper.repository.jpa.LinkRepositoryJpa;
import backend.academy.scrapper.util.LiquibaseMigration;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
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
@TestPropertySource(properties = "app.access-type=ORM")
@Import({TestcontainersConfiguration.class})
public class LinkRepositoryJpaTest {
    @Autowired
    private LinkRepository linkRepository;

    @Autowired
    private PostgreSQLContainer<?> postgresContainer;

    @PersistenceContext
    private EntityManager entityManager;

    private final Long telegramChatId = 12345L;
    private User testUser;
    private Link existingLink;

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

        testUser = new User();
        testUser.chatId(telegramChatId);
        entityManager.persist(testUser);

        Tag tag1 = new Tag();
        tag1.name("tech");
        tag1.telegramChatId(testUser);
        entityManager.persist(tag1);

        Tag tag2 = new Tag();
        tag2.name("news");
        tag2.telegramChatId(testUser);
        entityManager.persist(tag2);
        existingLink = new Link();
        existingLink.url("http://existing.com");
        existingLink.filters(Collections.singletonList("filter1"));
        existingLink.lastUpdatedAt(LocalDateTime.now(ZoneOffset.UTC));
        existingLink.linkType(LinkType.GITHUB);
        existingLink.telegramChatIds().add(testUser);
        entityManager.persist(existingLink);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @Transactional
    void testORMImplementation() {
        assertThat(linkRepository).isInstanceOf(LinkRepositoryJpa.class);
    }

    @Test
    @Transactional
    void testFindUserLinks() {
        Link newLink = new Link();
        newLink.url("http://example.com");
        newLink.filters(Collections.emptyList());
        newLink.lastUpdatedAt(LocalDateTime.now(ZoneOffset.UTC));
        newLink.linkType(LinkType.GITHUB);
        newLink.telegramChatIds().add(testUser);
        entityManager.persist(newLink);
        entityManager.flush();
        entityManager.clear();

        List<LinkDTO> userLinks = linkRepository.findUserLinks(telegramChatId);
        assertThat(userLinks.stream()
                        .map(LinkDTO::url)
                        .toList()
                        .containsAll(List.of("http://existing.com", "http://example.com")))
                .isEqualTo(true);
    }

    @Test
    @Transactional
    void testSaveNewLink() {
        String url = "https://github.com/nifreebie/Link_tracker_test";
        List<String> tags = Arrays.asList("tech", "news");
        List<String> filters = Arrays.asList("filterA", "filterB");
        LinkDTO savedLinkDTO = linkRepository.saveLink(url, tags, filters, telegramChatId);
        assertThat(savedLinkDTO).isNotNull();
        assertThat(savedLinkDTO.url()).isEqualTo(url);

        Link persistedLink = entityManager
                .createQuery("select l from Link l where l.url = :url", Link.class)
                .setParameter("url", url)
                .getSingleResult();
        assertThat(persistedLink.telegramChatIds().stream()
                        .map(User::chatId)
                        .toList()
                        .contains(telegramChatId))
                .isEqualTo(true);
        assertThat(persistedLink.tags().stream().map(Tag::name).toList().containsAll(List.of("tech", "news")))
                .isEqualTo(true);
    }

    @Test
    @Transactional
    void testSaveLink_UpdateExistingLink() {
        String url = existingLink.url();
        List<String> tags = Collections.singletonList("tech");
        List<String> filters = Collections.singletonList("newFilter");

        LinkDTO updatedLinkDTO = linkRepository.saveLink(url, tags, filters, telegramChatId);
        assertThat(updatedLinkDTO).isNotNull();
        assertThat(updatedLinkDTO.url()).isEqualTo(url);

        Link persistedLink = entityManager
                .createQuery("select l from Link l where l.url = :url", Link.class)
                .setParameter("url", url)
                .getSingleResult();
        assertThat(persistedLink.telegramChatIds().stream()
                        .map(User::chatId)
                        .toList()
                        .contains(telegramChatId))
                .isEqualTo(true);
        assertThat(persistedLink.tags().stream().map(Tag::name).toList().contains("tech"))
                .isEqualTo(true);
    }

    @Test
    @Transactional
    void testGetAll() {
        Link anotherLink = new Link();
        anotherLink.url("http://another.com");
        anotherLink.filters(Collections.emptyList());
        anotherLink.lastUpdatedAt(LocalDateTime.now(ZoneOffset.UTC));
        anotherLink.linkType(LinkType.GITHUB);
        entityManager.persist(anotherLink);
        entityManager.flush();
        entityManager.clear();

        List<LinkDTO> allLinks = linkRepository.getAll();
        assertThat(allLinks.stream()
                        .map(LinkDTO::url)
                        .toList()
                        .containsAll(List.of("http://another.com", "http://existing.com")))
                .isEqualTo(true);
    }

    @Test
    @Transactional
    void testIsUrlExists() {
        boolean exists = linkRepository.isUrlExists(existingLink.url());
        assertThat(exists).isTrue();

        boolean notExists = linkRepository.isUrlExists("http://nonexistent.com");
        assertThat(notExists).isFalse();
    }

    @Test
    @Transactional
    void testRemoveLinkByUrlAndTelegramChatId() {
        String url = existingLink.url();

        Link linkBefore = entityManager
                .createQuery("select l from Link l where l.url = :url", Link.class)
                .setParameter("url", url)
                .getSingleResult();
        assertThat(linkBefore.telegramChatIds().stream()
                        .map(User::chatId)
                        .toList()
                        .contains(telegramChatId))
                .isEqualTo(true);

        LinkDTO updatedLinkDTO = linkRepository.removeLinkByUrlAndTelegramChatId(url, telegramChatId);
        assertThat(updatedLinkDTO).isNotNull();

        Link linkAfter = entityManager
                .createQuery("select l from Link l where l.url = :url", Link.class)
                .setParameter("url", url)
                .getSingleResult();
        assertThat(linkAfter.telegramChatIds().stream()
                        .map(User::chatId)
                        .toList()
                        .contains(telegramChatId))
                .isEqualTo(false);
    }

    @Test
    @Transactional
    void testFindLinkByUrl() {
        String url = existingLink.url();
        LinkDTO foundLink = linkRepository.findLinkByUrl(url);
        assertThat(foundLink).isNotNull();
        assertThat(foundLink.url()).isEqualTo(url);
    }

    @Test
    @Transactional
    void testUpdateLastUpdatedAt() {
        LocalDateTime newTime = LocalDateTime.now(ZoneOffset.UTC).plusDays(1);
        Integer id = existingLink.id();
        linkRepository.updateLastUpdatedAt(id, newTime);
        Link updatedLink = entityManager.find(Link.class, id);
        assertThat(updatedLink.lastUpdatedAt()).isEqualTo(newTime);
    }
}
