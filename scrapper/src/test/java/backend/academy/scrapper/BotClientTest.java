package backend.academy.scrapper;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import backend.academy.scrapper.client.BotClient;
import backend.academy.scrapper.client.impl.BotClientImpl;
import backend.academy.scrapper.model.Link;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class BotClientTest {
    private WireMockServer wireMockServer;
    private BotClient botClient;

    @BeforeEach
    void setUp() {
        wireMockServer =
                new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMockServer.start();

        WireMock.configureFor("localhost", wireMockServer.port());

        botClient = new BotClientImpl();
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    void testUpdateWithBadRequest() {
        wireMockServer.stubFor(post(urlEqualTo("/api/v1/updates"))
                .willReturn(aResponse().withStatus(400).withBody("Bad Request")));

        Link link = new Link("https://example.com", List.of("tag"), List.of("filter"), 123);
        assertDoesNotThrow(() -> botClient.update(link));
    }

    @Test
    void testUpdateWithServerError() {
        wireMockServer.stubFor(post(urlEqualTo("/api/v1/updates"))
                .willReturn(aResponse().withStatus(500).withBody("Internal Server Error")));

        Link link = new Link("https://example.com", List.of("tag"), List.of("filter"), 123);
        assertDoesNotThrow(() -> botClient.update(link));
    }

    @Test
    void testUpdateWithInvalidResponseBody() {
        wireMockServer.stubFor(post(urlEqualTo("/api/v1/updates"))
                .willReturn(aResponse().withStatus(200).withBody("Invalid Response")));

        Link link = new Link("https://example.com", List.of("tag"), List.of("filter"), 123);
        assertDoesNotThrow(() -> botClient.update(link));
    }
}
