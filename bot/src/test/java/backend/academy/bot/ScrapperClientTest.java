package backend.academy.bot;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import backend.academy.bot.client.impl.ScrapperClientImpl;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest
public class ScrapperClientTest {
    private WireMockServer wireMockServer;
    private ScrapperClientImpl scrapperClient;

    @BeforeEach
    void setUp() {
        wireMockServer =
                new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMockServer.start();

        WireMock.configureFor("localhost", wireMockServer.port());

        scrapperClient = new ScrapperClientImpl("http://localhost:" + wireMockServer.port());
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    void testRegister_withConflict() {
        wireMockServer.stubFor(post(urlMatching("/tg-chat/.*"))
                .willReturn(aResponse()
                        .withStatus(409)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"exceptionMessage\":\"Конфликт\"}")));

        Mono<String> response = scrapperClient.register(123L);
        StepVerifier.create(response).expectNext("Конфликт").verifyComplete();
    }

    @Test
    void testGetUserLinks_success() {
        wireMockServer.stubFor(get(urlEqualTo("/links"))
                .withHeader("Tg-Chat-Id", equalTo("123"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"links\": []}")));

        Mono<?> response = scrapperClient.getUserLinks(123L);
        StepVerifier.create(response).expectNextMatches(Objects::nonNull).verifyComplete();
    }

    @Test
    void testTrack_withConflict() {
        wireMockServer.stubFor(post(urlEqualTo("/links"))
                .withHeader("Tg-Chat-Id", equalTo("123"))
                .willReturn(aResponse()
                        .withStatus(409)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"exceptionMessage\":\"Link already tracked\"}")));

        Mono<String> response = scrapperClient.track(123L, "http://example.com", List.of(), List.of());
        StepVerifier.create(response).expectNext("Link already tracked").verifyComplete();
    }

    @Test
    void testUntrack_withNotFound() {
        wireMockServer.stubFor(delete(urlEqualTo("/links"))
                .withHeader("Tg-Chat-Id", equalTo("123"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"exceptionMessage\":\"Link not found\"}")));

        Mono<String> response = scrapperClient.untrack(123L, "http://example.com");
        StepVerifier.create(response).expectNext("Link not found").verifyComplete();
    }
}
