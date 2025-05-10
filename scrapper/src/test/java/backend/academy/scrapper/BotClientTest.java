package backend.academy.scrapper;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import backend.academy.scrapper.client.impl.BotClientImpl;
import backend.academy.scrapper.model.domain.EventType;
import backend.academy.scrapper.model.domain.LinkType;
import backend.academy.scrapper.model.dto.EventDTO;
import backend.academy.scrapper.model.dto.LinkDTO;
import backend.academy.scrapper.util.LiquibaseMigration;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
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
@TestPropertySource(properties = {"app.access-type=ORM"})
@Import({TestcontainersConfiguration.class})
public class BotClientTest {
    private WireMockServer wireMockServer;
    private LinkDTO link;
    private EventDTO event;

    @Autowired
    private BotClientImpl botClient;

    @Autowired
    private PostgreSQLContainer<?> postgresContainer;

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

        wireMockServer =
                new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMockServer.start();

        WireMock.configureFor("localhost", wireMockServer.port());

        link = new LinkDTO(
                1,
                "https://github.com/nifreebie/Link_tracker_test",
                List.of("tag"),
                List.of("filter"),
                LocalDateTime.now(),
                List.of(1L),
                LinkType.GITHUB);
        event = new EventDTO("commit", "user", LocalDateTime.now(), "", EventType.COMMIT);
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    void testUpdateWithBadRequest() {
        wireMockServer.stubFor(post(urlEqualTo("/api/v1/updates"))
                .willReturn(aResponse().withStatus(400).withBody("Bad Request")));

        assertDoesNotThrow(() -> botClient.update(link, event));
    }

    @Test
    void testUpdateWithServerError() {
        wireMockServer.stubFor(post(urlEqualTo("/api/v1/updates"))
                .willReturn(aResponse().withStatus(500).withBody("Internal Server Error")));

        assertDoesNotThrow(() -> botClient.update(link, event));
    }

    @Test
    void testUpdateWithInvalidResponseBody() {
        wireMockServer.stubFor(post(urlEqualTo("/api/v1/updates"))
                .willReturn(aResponse().withStatus(200).withBody("Invalid Response")));

        assertDoesNotThrow(() -> botClient.update(link, event));
    }
}
