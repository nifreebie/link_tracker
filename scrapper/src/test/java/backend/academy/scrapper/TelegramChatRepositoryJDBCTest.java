package backend.academy.scrapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import backend.academy.scrapper.repository.TelegramChatRepository;
import backend.academy.scrapper.util.LiquibaseMigration;
import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest
@TestPropertySource(properties = "app.access-type=SQL")
@Import({TestcontainersConfiguration.class})
public class TelegramChatRepositoryJDBCTest {
    @Autowired
    private TelegramChatRepository telegramChatRepository;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private PostgreSQLContainer<?> postgresContainer;

    private Long testChatId;

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

        testChatId = 99999L;
    }

    @Test
    void testSaveChat() {
        telegramChatRepository.saveChat(testChatId);
        Integer count = telegramChatRepository.countChatId(testChatId);
        assertNotNull(count);
        assertEquals(1, count);
        List<Long> users = telegramChatRepository.findAllUsers();
        assertNotNull(users);
        assertTrue(users.contains(testChatId));
    }

    @Test
    void testRemoveChat() {
        telegramChatRepository.saveChat(testChatId);
        Integer count = telegramChatRepository.countChatId(testChatId);
        assertEquals(1, count);
        telegramChatRepository.removeChat(testChatId);
        Integer countAfter = telegramChatRepository.countChatId(testChatId);
        assertEquals(0, countAfter);
        List<Long> users = telegramChatRepository.findAllUsers();
        assertFalse(users.contains(testChatId));
    }

    @Test
    void testCountChatId() {
        Integer initialCount = telegramChatRepository.countChatId(testChatId);
        assertEquals(0, initialCount);
        telegramChatRepository.saveChat(testChatId);
        Integer count = telegramChatRepository.countChatId(testChatId);
        assertEquals(1, count);
    }

    @Test
    void testFindAllUsers() {
        telegramChatRepository.saveChat(testChatId);
        telegramChatRepository.saveChat(88888L);
        telegramChatRepository.saveChat(77777L);
        List<Long> users = telegramChatRepository.findAllUsers();
        assertTrue(users.contains(testChatId));
        assertTrue(users.contains(88888L));
        assertTrue(users.contains(77777L));
        assertEquals(3, users.size());
    }
}
