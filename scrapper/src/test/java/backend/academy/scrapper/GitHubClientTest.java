package backend.academy.scrapper;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import backend.academy.scrapper.client.impl.GithubClientImpl;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest
public class GitHubClientTest {
    private static WireMockServer wireMockServer;
    private static GithubClientImpl gitHubClient;

    @Autowired
    private ScrapperConfig config;

    @BeforeEach
    void setUp() {
        wireMockServer =
            new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMockServer.start();

        configureFor("localhost", wireMockServer.port());

        gitHubClient = new GithubClientImpl("http://localhost:" + wireMockServer.port(), config);
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    void testGetRepoLastUpdatedWithBadRequest() {
        wireMockServer.stubFor(get(urlMatching("/repos/.*/.*"))
            .willReturn(aResponse()
                .withStatus(400)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"message\":\"Bad Request\"}")));

        Mono<String> response = gitHubClient.getRepoLastUpdated("owner", "repo");
        StepVerifier.create(response)
            .expectErrorMatches(throwable -> throwable.getMessage().contains("Bad request"))
            .verify();
    }

    @Test
    void testGetRepoLastUpdatedWithNotFound() {
        wireMockServer.stubFor(get(urlMatching("/repos/.*/.*"))
            .willReturn(aResponse()
                .withStatus(404)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"message\":\"Not Found\"}")));

        Mono<String> response = gitHubClient.getRepoLastUpdated("owner", "repo");
        StepVerifier.create(response)
            .expectErrorMatches(throwable -> throwable.getMessage().contains("Not found"))
            .verify();
    }

    @Test
    void testGetRepoLastUpdatedWithServerError() {
        wireMockServer.stubFor(get(urlMatching("/repos/.*/.*"))
            .willReturn(aResponse()
                .withStatus(500)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"message\":\"Internal Server Error\"}")));

        Mono<String> response = gitHubClient.getRepoLastUpdated("owner", "repo");
        StepVerifier.create(response)
            .expectErrorMatches(throwable -> throwable.getMessage().contains("Internal server error"))
            .verify();
    }

    @Test
    void testGetRepoLastUpdatedWithValidResponse() {
        String jsonResponse = "{\"updated_at\": \"2024-03-01T12:00:00Z\"}";
        wireMockServer.stubFor(get(urlMatching("/repos/.*/.*"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(jsonResponse)));

        Mono<String> response = gitHubClient.getRepoLastUpdated("owner", "repo");
        StepVerifier.create(response).expectNext("2024-03-01T12:00:00Z").verifyComplete();
    }
}
