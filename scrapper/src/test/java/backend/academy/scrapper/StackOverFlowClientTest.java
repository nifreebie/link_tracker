package backend.academy.scrapper;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;

import backend.academy.scrapper.client.impl.StackOverFlowClientImpl;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest
public class StackOverFlowClientTest {
    private WireMockServer wireMockServer;
    private StackOverFlowClientImpl stackOverFlowClient;

    @Autowired
    private ScrapperConfig config;

    @BeforeEach
    void setUp() {
        wireMockServer =
                new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMockServer.start();

        WireMock.configureFor("localhost", wireMockServer.port());

        stackOverFlowClient = new StackOverFlowClientImpl("http://localhost:" + wireMockServer.port(), config);
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    void testGetQuestionLastUpdatedWithBadRequest() {
        wireMockServer.stubFor(get(urlMatching("/questions/.*"))
                .willReturn(aResponse().withStatus(400).withBody("Bad Request")));

        Mono<String> response = stackOverFlowClient.getQuestionLastUpdated("12345");
        StepVerifier.create(response)
                .expectErrorMatches(throwable -> throwable.getMessage().contains("Bad request"))
                .verify();
    }

    @Test
    void testGetQuestionLastUpdatedWithNotFound() {
        wireMockServer.stubFor(get(urlMatching("/questions/.*"))
                .willReturn(aResponse().withStatus(404).withBody("Not Found")));

        Mono<String> response = stackOverFlowClient.getQuestionLastUpdated("12345");
        StepVerifier.create(response)
                .expectErrorMatches(throwable -> throwable.getMessage().contains("Not found"))
                .verify();
    }

    @Test
    void testGetQuestionLastUpdatedWithServerError() {
        wireMockServer.stubFor(get(urlMatching("/questions/.*"))
                .willReturn(aResponse().withStatus(500).withBody("Internal Server Error")));

        Mono<String> response = stackOverFlowClient.getQuestionLastUpdated("12345");
        StepVerifier.create(response)
                .expectErrorMatches(throwable -> throwable.getMessage().contains("Internal server error"))
                .verify();
    }
}
