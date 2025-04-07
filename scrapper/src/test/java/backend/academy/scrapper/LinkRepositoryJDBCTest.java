package backend.academy.scrapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import backend.academy.scrapper.model.domain.LinkType;
import backend.academy.scrapper.model.dto.LinkDTO;
import backend.academy.scrapper.repository.LinkRepository;
import backend.academy.scrapper.util.LiquibaseMigration;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest
@TestPropertySource(properties = "app.access-type=SQL")
@Import({TestcontainersConfiguration.class})
public class LinkRepositoryJDBCTest {
    @Autowired
    private LinkRepository linkRepository;

    @Autowired
    private PostgreSQLContainer<?> postgresContainer;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    private Long telegramChatId;
    private String testUrl;

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.PostgreSQLDialect");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
    }

    @BeforeEach
    void setUp() {
        LiquibaseMigration.migrate(
                Paths.get("../migrations/master.xml"),
                postgresContainer.getUsername(),
                postgresContainer.getPassword(),
                postgresContainer.getJdbcUrl());

        jdbcTemplate.update("DELETE FROM link_tag", EmptySqlParameterSource.INSTANCE);
        jdbcTemplate.update("DELETE FROM user_link", EmptySqlParameterSource.INSTANCE);
        jdbcTemplate.update("DELETE FROM tags", EmptySqlParameterSource.INSTANCE);
        jdbcTemplate.update("DELETE FROM links", EmptySqlParameterSource.INSTANCE);
        jdbcTemplate.update("DELETE FROM users", EmptySqlParameterSource.INSTANCE);

        telegramChatId = 12345L;
        testUrl = "https://github.com/nifreebie/Link_tracker_test";
        String insertUserSql = "INSERT INTO users (chat_id) VALUES (:chatId) RETURNING id";
        MapSqlParameterSource userParams = new MapSqlParameterSource("chatId", telegramChatId);
        Long testUserId = jdbcTemplate.queryForObject(insertUserSql, userParams, Long.class);
        String insertTagSql = "INSERT INTO tags (name, user_id) VALUES (:name, :userId) RETURNING id";
        MapSqlParameterSource tagParams1 = new MapSqlParameterSource("name", "tech");
        tagParams1.addValue("userId", testUserId);
        Long tagId1 = jdbcTemplate.queryForObject(insertTagSql, tagParams1, Long.class);

        MapSqlParameterSource tagParams2 = new MapSqlParameterSource("name", "news");
        tagParams2.addValue("userId", testUserId);
        Long tagId2 = jdbcTemplate.queryForObject(insertTagSql, tagParams2, Long.class);
        String insertLinkSql = "INSERT INTO links (url, filters, last_updated_at, type) "
                + "VALUES (:url, :filters, :lastUpdatedAt, :linkType) RETURNING id";
        String filters = "filter1";
        MapSqlParameterSource linkParams = new MapSqlParameterSource()
                .addValue("url", testUrl)
                .addValue("filters", filters)
                .addValue("lastUpdatedAt", LocalDateTime.now(ZoneOffset.UTC))
                .addValue("linkType", "GITHUB");
        Integer existingLinkId = jdbcTemplate.queryForObject(insertLinkSql, linkParams, Integer.class);
        String insertUserLinkSql = "INSERT INTO user_link (link_id, user_id) VALUES (:linkId, :userId)";
        MapSqlParameterSource userLinkParams =
                new MapSqlParameterSource().addValue("linkId", existingLinkId).addValue("userId", testUserId);
        jdbcTemplate.update(insertUserLinkSql, userLinkParams);
        String insertLinkTagSql = "INSERT INTO link_tag (link_id, tag_id) VALUES (:linkId, :tagId)";
        MapSqlParameterSource tagLinkParams1 =
                new MapSqlParameterSource().addValue("linkId", existingLinkId).addValue("tagId", tagId1);
        jdbcTemplate.update(insertLinkTagSql, tagLinkParams1);

        MapSqlParameterSource tagLinkParams2 =
                new MapSqlParameterSource().addValue("linkId", existingLinkId).addValue("tagId", tagId2);
        jdbcTemplate.update(insertLinkTagSql, tagLinkParams2);
    }

    @Test
    void testSaveNewLink() {
        List<String> tags = List.of("tech");
        List<String> filters = List.of("filter1");

        LinkDTO savedLink = linkRepository.saveLink(testUrl, tags, filters, telegramChatId);

        assertNotNull(savedLink);
        assertEquals(testUrl, savedLink.url());
        assertEquals(LinkType.GITHUB, savedLink.linkType());
        assertTrue(savedLink.filters().contains("filter1"));
    }

    @Test
    void testSaveExistingLink() {
        List<String> tags = List.of("tech");
        List<String> filters = List.of("filter1");

        linkRepository.saveLink(testUrl, tags, filters, telegramChatId);
        LinkDTO savedLink = linkRepository.saveLink(testUrl, tags, filters, telegramChatId);

        assertNotNull(savedLink);
        assertEquals(testUrl, savedLink.url());
        assertEquals(LinkType.getLinkType(testUrl), savedLink.linkType());
        assertTrue(savedLink.filters().contains("filter1"));
    }

    @Test
    void testFindUserLinks() {
        List<String> tags = List.of("tech");
        List<String> filters = List.of("filter1");
        linkRepository.saveLink(testUrl, tags, filters, telegramChatId);
        List<LinkDTO> links = linkRepository.findUserLinks(telegramChatId);

        assertNotNull(links);
        assertFalse(links.isEmpty());
        assertEquals(testUrl, links.getFirst().url());
    }

    @Test
    void testRemoveLinkByUrlAndTelegramChatId() {
        List<String> tags = List.of("tech");
        List<String> filters = List.of("filter1");
        linkRepository.saveLink(testUrl, tags, filters, telegramChatId);

        LinkDTO removedLink = linkRepository.removeLinkByUrlAndTelegramChatId(testUrl, telegramChatId);

        assertNotNull(removedLink);
        assertEquals(testUrl, removedLink.url());

        List<LinkDTO> links = linkRepository.findUserLinks(telegramChatId);
        assertTrue(links.isEmpty());
    }

    @Test
    void testIsUrlExists() {
        List<String> tags = List.of("tech");
        List<String> filters = List.of("filter1");
        linkRepository.saveLink(testUrl, tags, filters, telegramChatId);

        boolean exists = linkRepository.isUrlExists(testUrl);
        assertTrue(exists);

        boolean notExists = linkRepository.isUrlExists("http://nonexistent.com");
        assertFalse(notExists);
    }

    @Test
    void testUpdateLastUpdatedAt() {
        List<String> tags = List.of("tech");
        List<String> filters = List.of("filter1");
        LinkDTO savedLink = linkRepository.saveLink(testUrl, tags, filters, telegramChatId);

        LocalDateTime newDate = LocalDateTime.now(ZoneOffset.UTC).plusDays(1);
        linkRepository.updateLastUpdatedAt(savedLink.id(), newDate);

        LinkDTO updatedLink = linkRepository.findLinkByUrl(testUrl);
        assertNotNull(updatedLink);
        assertEquals(
                newDate.truncatedTo(ChronoUnit.MILLIS),
                updatedLink.lastUpdatedAt().truncatedTo(ChronoUnit.MILLIS));
    }
}
