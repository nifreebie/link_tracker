package backend.academy.scrapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import backend.academy.scrapper.repository.TagRepository;
import backend.academy.scrapper.util.LiquibaseMigration;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
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
public class TagRepositoryJDBCTest {
    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private PostgreSQLContainer<?> postgresContainer;

    private Long telegramChatId;
    private String testUrl;
    private Long testUserId;
    private Integer existingLinkId;

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
        testUserId = jdbcTemplate.queryForObject(insertUserSql, userParams, Long.class);
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
        existingLinkId = jdbcTemplate.queryForObject(insertLinkSql, linkParams, Integer.class);
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
    void testCreateTag() {
        String newTagName = "innovation";
        String createdTagName = tagRepository.createTag(newTagName, telegramChatId);
        assertNotNull(createdTagName);
        assertEquals(newTagName, createdTagName);

        List<String> userTags = tagRepository.getUserTags(telegramChatId);
        assertTrue(userTags.contains(newTagName));
    }

    @Test
    void testGetUserTags() {
        List<String> tags = tagRepository.getUserTags(telegramChatId);
        assertTrue(tags.contains("tech"));
        assertTrue(tags.contains("news"));
    }

    @Test
    void testChangeLinkTags() {
        String additionalTag = "feature";
        tagRepository.createTag(additionalTag, telegramChatId);
        List<String> userTags = tagRepository.getUserTags(telegramChatId);
        assertTrue(userTags.contains(additionalTag));

        tagRepository.addLinkTag(additionalTag, testUrl, telegramChatId);
        String linkTagSql = "SELECT COUNT(*) FROM link_tag lt " + "WHERE lt.link_id = :linkId "
                + "AND lt.tag_id = (SELECT id FROM tags WHERE name = :tagName AND user_id = :userId)";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("linkId", existingLinkId)
                .addValue("tagName", additionalTag)
                .addValue("userId", testUserId);
        Integer count = jdbcTemplate.queryForObject(linkTagSql, params, Integer.class);
        assertNotNull(count);
        assertTrue(count > 0);
        tagRepository.removeLinkTag(additionalTag, testUrl, telegramChatId);
        Integer countAfter = jdbcTemplate.queryForObject(linkTagSql, params, Integer.class);
        assertEquals(0, countAfter);
    }

    @Test
    void testIsTagExists() {
        assertTrue(tagRepository.isTagExists("tech"));
        assertFalse(tagRepository.isTagExists("nonexistent"));
    }
}
