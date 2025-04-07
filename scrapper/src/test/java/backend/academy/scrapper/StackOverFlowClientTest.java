package backend.academy.scrapper;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;

import backend.academy.scrapper.client.impl.StackOverFlowClientImpl;
import backend.academy.scrapper.model.domain.EventType;
import backend.academy.scrapper.util.LiquibaseMigration;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.nio.file.Paths;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import reactor.test.StepVerifier;

@SpringBootTest(
        properties = {"app.github-token=test", "app.stackoverflow.key=test", "app.stackoverflow.access-token=test"})
@TestPropertySource(properties = "app.access-type=ORM")
@Import({TestcontainersConfiguration.class})
public class StackOverFlowClientTest {
    private WireMockServer wireMockServer;
    private StackOverFlowClientImpl stackOverFlowClient;

    @Autowired
    private PostgreSQLContainer<?> postgresContainer;

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.PostgreSQLDialect");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
    }

    @Autowired
    private ScrapperConfig config;

    @BeforeEach
    void setUp() {
        LiquibaseMigration.migrate(
                Paths.get("../migrations/master.xml"),
                postgresContainer.getUsername(),
                postgresContainer.getPassword(),
                postgresContainer.getJdbcUrl());

        wireMockServer =
                new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMockServer.start();

        configureFor("localhost", wireMockServer.port());

        stackOverFlowClient = new StackOverFlowClientImpl("http://localhost:" + wireMockServer.port(), config);
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    void testGetQuestionLastAnswerSuccess() {
        String responseJson =
                """
                    {
                      "items": [
                        {
                          "creation_date": 1712212000,
                          "owner": {
                            "display_name": "AnswerUser"
                          },
                          "body": "This is a test answer"
                        }
                      ]
                    }
                """;

        stubFor(get(urlPathMatching("/questions/12345/answers"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseJson)));

        StepVerifier.create(stackOverFlowClient.getQuestionLastAnswer("12345"))
                .expectNextMatches(event -> event.username().equals("AnswerUser")
                        && event.description().contains("test answer")
                        && event.eventType().equals(EventType.ANSWER))
                .verifyComplete();
    }

    @Test
    void testGetQuestionLastCommentSuccess() {
        String responseJson =
                """
                    {
                      "items": [
                        {
                          "creation_date": 1712213000,
                          "owner": {
                            "display_name": "CommentUser"
                          },
                          "body": "This is a test comment"
                        }
                      ]
                    }
                """;

        stubFor(get(urlPathMatching("/questions/12345/comments"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseJson)));

        StepVerifier.create(stackOverFlowClient.getQuestionLastComment("12345"))
                .expectNextMatches(event -> event.username().equals("CommentUser")
                        && event.description().contains("test comment")
                        && event.eventType().equals(EventType.COMMENT))
                .verifyComplete();
    }

    @Test
    void testGetQuestionLastAnswerWithNotFound() {
        stubFor(get(urlPathMatching("/questions/.*?/answers"))
                .willReturn(aResponse().withStatus(404).withBody("Not Found")));

        StepVerifier.create(stackOverFlowClient.getQuestionLastAnswer("12345"))
                .expectErrorMatches(error -> error.getMessage().contains("404"))
                .verify();
    }

    @Test
    void testGetQuestionLastCommentWithServerError() {
        stubFor(get(urlPathMatching("/questions/.*?/comments"))
                .willReturn(aResponse().withStatus(500).withBody("Internal Server Error")));

        StepVerifier.create(stackOverFlowClient.getQuestionLastComment("12345"))
                .expectErrorMatches(error -> error.getMessage().contains("500"))
                .verify();
    }
}
