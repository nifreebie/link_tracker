package backend.academy.scrapper;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;

import backend.academy.scrapper.client.impl.BotClientImpl;
import backend.academy.scrapper.model.domain.EventType;
import backend.academy.scrapper.model.domain.LinkType;
import backend.academy.scrapper.model.dto.EventDTO;
import backend.academy.scrapper.model.dto.LinkDTO;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.http.Fault;
import java.time.LocalDateTime;
import java.util.List;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@TestPropertySource(properties = {"app.access-type=ORM", "app.message-transport=HTTP"})
public class BotClientRetryTest {
    private static WireMockServer wireMockServer =
            new WireMockServer(WireMockConfiguration.options().dynamicPort());

    private LinkDTO link;
    private EventDTO event;

    @Autowired
    private BotClientImpl botClient;

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        wireMockServer.start();
        String base = "http://localhost:" + wireMockServer.port();
        registry.add("app.bot-api-url", () -> base + "/api/v1");
    }

    @BeforeEach
    @Transactional
    void setUp() {

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

    @AfterAll
    static void cleanup() {
        wireMockServer.stop();
    }

    @Test
    void retryOn5xxThenSuccess() {
        wireMockServer.resetAll();

        stubFor(post("/api/v1/updates")
                .inScenario("5xx")
                .whenScenarioStateIs(STARTED)
                .willReturn(aResponse().withStatus(500))
                .willSetStateTo("TRY2"));

        stubFor(post("/api/v1/updates")
                .inScenario("5xx")
                .whenScenarioStateIs("TRY2")
                .willReturn(aResponse().withStatus(500))
                .willSetStateTo("TRY3"));

        stubFor(post("/api/v1/updates")
                .inScenario("5xx")
                .whenScenarioStateIs("TRY3")
                .willReturn(aResponse().withStatus(200).withBody("OK")));

        botClient.update(link, event);

        Awaitility.await()
                .atMost(java.time.Duration.ofSeconds(10))
                .untilAsserted(() -> wireMockServer.verify(3, postRequestedFor(urlEqualTo("/api/v1/updates"))));
    }

    @Test
    void retryOn429ThenSuccess() {
        wireMockServer.resetAll();

        stubFor(post("/api/v1/updates")
                .inScenario("429")
                .whenScenarioStateIs(STARTED)
                .willReturn(aResponse().withStatus(429))
                .willSetStateTo("TRY2"));

        stubFor(post("/api/v1/updates")
                .inScenario("429")
                .whenScenarioStateIs("TRY2")
                .willReturn(aResponse().withStatus(429))
                .willSetStateTo("TRY3"));

        stubFor(post("/api/v1/updates")
                .inScenario("429")
                .whenScenarioStateIs("TRY3")
                .willReturn(aResponse().withStatus(200).withBody("OK")));

        botClient.update(link, event);

        Awaitility.await()
                .atMost(java.time.Duration.ofSeconds(10))
                .untilAsserted(() -> wireMockServer.verify(3, postRequestedFor(urlEqualTo("/api/v1/updates"))));
    }

    @Test
    void retryOnIOException() {
        wireMockServer.resetAll();

        stubFor(post("/api/v1/updates")
                .inScenario("IO")
                .whenScenarioStateIs(STARTED)
                .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER))
                .willSetStateTo("OK"));

        stubFor(post("/api/v1/updates")
                .inScenario("IO")
                .whenScenarioStateIs("OK")
                .willReturn(aResponse().withStatus(200).withBody("OK")));

        botClient.update(link, event);

        Awaitility.await()
                .atMost(java.time.Duration.ofSeconds(10))
                .untilAsserted(() -> wireMockServer.verify(2, postRequestedFor(urlEqualTo("/api/v1/updates"))));
    }

    @Test
    void noRetryOn4xx() {
        wireMockServer.resetAll();

        stubFor(post("/api/v1/updates").willReturn(aResponse().withStatus(400)));

        botClient.update(link, event);

        Awaitility.await()
                .atMost(java.time.Duration.ofSeconds(5))
                .untilAsserted(() -> wireMockServer.verify(1, postRequestedFor(urlEqualTo("/api/v1/updates"))));
    }
}
