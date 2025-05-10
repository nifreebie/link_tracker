package backend.academy.scrapper;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertTrue;

import backend.academy.scrapper.client.impl.BotClientImpl;
import backend.academy.scrapper.model.domain.EventType;
import backend.academy.scrapper.model.domain.LinkType;
import backend.academy.scrapper.model.dto.EventDTO;
import backend.academy.scrapper.model.dto.LinkDTO;
import backend.academy.scrapper.model.dto.request.LinkUpdateRequest;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.test.StepVerifier;

@SpringBootTest(
        properties = {
            "app.connection-timeout=500ms",
            "app.response-timeout=500ms",
            "app.max-retries=0",
            "app.backoff=0ms",
            "app.message-transport=HTTP"
        })
public class CircuitBreakerTest {
    private static WireMockServer wireMockServer =
            new WireMockServer(WireMockConfiguration.options().dynamicPort());

    private LinkDTO link;
    private EventDTO event;
    private LinkUpdateRequest request;

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
        request = new LinkUpdateRequest(
                link.id(),
                link.url(),
                event.description(),
                link.telegramChatIds(),
                event.title(),
                event.username(),
                event.date(),
                event.eventType());
    }

    @Test
    void whenServiceTooSlow_thenCircuitBreakerFailsFast() {
        stubFor(post(urlEqualTo("/api/v1/updates"))
                .willReturn(aResponse().withFixedDelay(2000).withStatus(200).withBody("OK")));

        long start = System.currentTimeMillis();

        StepVerifier.create(botClient.notifyUpdate(request, link))
                .expectError(WebClientRequestException.class)
                .verify();

        long elapsed = System.currentTimeMillis() - start;
        assertTrue(elapsed < 1000);
    }
}
