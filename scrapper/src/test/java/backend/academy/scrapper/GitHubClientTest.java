package backend.academy.scrapper;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;

import backend.academy.scrapper.client.impl.GithubClientImpl;
import backend.academy.scrapper.model.domain.EventType;
import backend.academy.scrapper.model.dto.EventDTO;
import backend.academy.scrapper.util.LiquibaseMigration;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest(
        properties = {"app.github-token=test", "app.stackoverflow.key=test", "app.stackoverflow.access-token=test"})
@TestPropertySource(properties = "app.access-type=ORM")
@Import({TestcontainersConfiguration.class})
public class GitHubClientTest {
    private static WireMockServer wireMockServer =
            new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());

    @Autowired
    private GithubClientImpl gitHubClient;

    @Autowired
    private PostgreSQLContainer<?> postgresContainer;

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        wireMockServer.start();
        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.PostgreSQLDialect");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
        String base = "http://localhost:" + wireMockServer.port();
        registry.add("app.github-api-url", () -> base);
    }

    @BeforeEach
    void setUp() {
        LiquibaseMigration.migrate(
                Paths.get("../migrations/master.xml"),
                postgresContainer.getUsername(),
                postgresContainer.getPassword(),
                postgresContainer.getJdbcUrl());

        configureFor("localhost", wireMockServer.port());
    }

    @Test
    void testGetRepoLastUpdatedWithBadRequest() {
        wireMockServer.stubFor(get(urlMatching("/repos/.*/.*"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"message\":\"Bad Request\"}")));

        Mono<EventDTO> response = gitHubClient.getRepoLastUpdated("owner", "repo");
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

        Mono<EventDTO> response = gitHubClient.getRepoLastUpdated("owner", "repo");
        StepVerifier.create(response)
                .expectErrorMatches(throwable -> throwable.getMessage().contains("Not found"))
                .verify();
    }

    @Test
    void testGetRepoLastUpdatedWithValidResponse() {
        String jsonResponse = "[{\n" + "        \"sha\": \"3a1b5aee08d6b474af34f6c83c9f5c46fe1e2f04\",\n"
                + "        \"node_id\": \"C_kwDON7xx0doAKDNhMWI1YWVlMDhkNmI0NzRhZjM0ZjZjODNjOWY1YzQ2ZmUxZTJmMDQ\",\n"
                + "        \"commit\": {\n"
                + "            \"author\": {\n"
                + "                \"name\": \"Nikita Isupov\",\n"
                + "                \"email\": \"145199579+nifreebie@users.noreply.github.com\",\n"
                + "                \"date\": \"2024-03-01T12:00:00Z\"\n"
                + "            },\n"
                + "            \"committer\": {\n"
                + "                \"name\": \"GitHub\",\n"
                + "                \"email\": \"noreply@github.com\",\n"
                + "                \"date\": \"2025-03-16T18:24:08Z\"\n"
                + "            },\n"
                + "            \"message\": \"Update README.md\",\n"
                + "            \"tree\": {\n"
                + "                \"sha\": \"044e7fdcf62204460d1d22f39803a20731541e3b\",\n"
                + "                \"url\": \"https://api.github.com/repos/nifreebie/Link_tracker_test/git/trees/044e7fdcf62204460d1d22f39803a20731541e3b\"\n"
                + "            },\n"
                + "            \"url\": \"https://api.github.com/repos/nifreebie/Link_tracker_test/git/commits/3a1b5aee08d6b474af34f6c83c9f5c46fe1e2f04\",\n"
                + "            \"comment_count\": 0,\n"
                + "            \"verification\": {\n"
                + "                \"verified\": true,\n"
                + "                \"reason\": \"valid\",\n"
                + "                \"signature\": \"-----BEGIN PGP SIGNATURE-----\\n\\nwsFcBAABCAAQBQJn1xdICRC1aQ7uu5UhlAAAQ+0QACllqVx4Zsirp8wtIw4co4gn\\npZUR1BrRg6NFSB8Pu5+Hy7GtVzFn2NzORnWMMfrfOCYutayjP+QXdM8jWJNg7Eq2\\nXxTL7I9Jr5JCHSnEGSQLVOAREz57CNR/S1BB1yM4Z5d3gCJMtNkETU12Tu5De3Xg\\nEQCc+cDJT5GAPjA0AdAZF07+YrBTXbhReNgKBWkTg6t3G/+nIQWZ/jjv8hwZTCcB\\n4T6A2YZVlaN8JPupqDOspSqdaGi9LXzF2wnh0kuiYdodORdxM/TBsDQK9AWERmXU\\nb70DK5BWrzgaG7LCbfOZqG04mB1voWakwAlfEHWPzm7unAa+5VV0Vicq3mE3KZ08\\nJuAYenR7LvYWm/RGIlH81XNVm+0hSbNbB1cd+UbpnVexuRYVaNfX6weNfM9FvGPS\\nxOMLEsnhfTU62AG96h+Iid36lJgrjZQ1eNEAvPEH2bwy/KKDVdpGYjSaP1yc723f\\n48dkU7G1lIQIvW5UpcoqMEK9bziNCwqX7sksM73ya6s4mQWhf+0lfO4mzYAk3HZg\\nT3AiX7rmY4riSWthgH7FItluGHW5dbTaPy+znOFFSLE7v3PptmS/wxAggfQ8V0Md\\nu9F8cahmSD6f9C2WXejw/diIMdIBEFpkxTvjdKh357k0Ej11VPDNyl7dFLlaU4ca\\n7Q/C9bIE/8mPwl0eZrpy\\n=fGOK\\n-----END PGP SIGNATURE-----\\n\",\n"
                + "                \"payload\": \"tree 044e7fdcf62204460d1d22f39803a20731541e3b\\nparent 2c3b2d336f11d375430257bf9a89206efab79ba2\\nauthor Nikita Isupov <145199579+nifreebie@users.noreply.github.com> 1742149448 +0300\\ncommitter GitHub <noreply@github.com> 1742149448 +0300\\n\\nUpdate README.md\",\n"
                + "                \"verified_at\": \"2025-03-16T18:24:14Z\"\n"
                + "            }\n"
                + "        },\n"
                + "        \"url\": \"https://api.github.com/repos/nifreebie/Link_tracker_test/commits/3a1b5aee08d6b474af34f6c83c9f5c46fe1e2f04\",\n"
                + "        \"html_url\": \"https://github.com/nifreebie/Link_tracker_test/commit/3a1b5aee08d6b474af34f6c83c9f5c46fe1e2f04\",\n"
                + "        \"comments_url\": \"https://api.github.com/repos/nifreebie/Link_tracker_test/commits/3a1b5aee08d6b474af34f6c83c9f5c46fe1e2f04/comments\",\n"
                + "        \"author\": {\n"
                + "            \"login\": \"nifreebie\",\n"
                + "            \"id\": 145199579,\n"
                + "            \"node_id\": \"U_kgDOCKeR2w\",\n"
                + "            \"avatar_url\": \"https://avatars.githubusercontent.com/u/145199579?v=4\",\n"
                + "            \"gravatar_id\": \"\",\n"
                + "            \"url\": \"https://api.github.com/users/nifreebie\",\n"
                + "            \"html_url\": \"https://github.com/nifreebie\",\n"
                + "            \"followers_url\": \"https://api.github.com/users/nifreebie/followers\",\n"
                + "            \"following_url\": \"https://api.github.com/users/nifreebie/following{/other_user}\",\n"
                + "            \"gists_url\": \"https://api.github.com/users/nifreebie/gists{/gist_id}\",\n"
                + "            \"starred_url\": \"https://api.github.com/users/nifreebie/starred{/owner}{/repo}\",\n"
                + "            \"subscriptions_url\": \"https://api.github.com/users/nifreebie/subscriptions\",\n"
                + "            \"organizations_url\": \"https://api.github.com/users/nifreebie/orgs\",\n"
                + "            \"repos_url\": \"https://api.github.com/users/nifreebie/repos\",\n"
                + "            \"events_url\": \"https://api.github.com/users/nifreebie/events{/privacy}\",\n"
                + "            \"received_events_url\": \"https://api.github.com/users/nifreebie/received_events\",\n"
                + "            \"type\": \"User\",\n"
                + "            \"user_view_type\": \"public\",\n"
                + "            \"site_admin\": false\n"
                + "        },\n"
                + "        \"committer\": {\n"
                + "            \"login\": \"web-flow\",\n"
                + "            \"id\": 19864447,\n"
                + "            \"node_id\": \"MDQ6VXNlcjE5ODY0NDQ3\",\n"
                + "            \"avatar_url\": \"https://avatars.githubusercontent.com/u/19864447?v=4\",\n"
                + "            \"gravatar_id\": \"\",\n"
                + "            \"url\": \"https://api.github.com/users/web-flow\",\n"
                + "            \"html_url\": \"https://github.com/web-flow\",\n"
                + "            \"followers_url\": \"https://api.github.com/users/web-flow/followers\",\n"
                + "            \"following_url\": \"https://api.github.com/users/web-flow/following{/other_user}\",\n"
                + "            \"gists_url\": \"https://api.github.com/users/web-flow/gists{/gist_id}\",\n"
                + "            \"starred_url\": \"https://api.github.com/users/web-flow/starred{/owner}{/repo}\",\n"
                + "            \"subscriptions_url\": \"https://api.github.com/users/web-flow/subscriptions\",\n"
                + "            \"organizations_url\": \"https://api.github.com/users/web-flow/orgs\",\n"
                + "            \"repos_url\": \"https://api.github.com/users/web-flow/repos\",\n"
                + "            \"events_url\": \"https://api.github.com/users/web-flow/events{/privacy}\",\n"
                + "            \"received_events_url\": \"https://api.github.com/users/web-flow/received_events\",\n"
                + "            \"type\": \"User\",\n"
                + "            \"user_view_type\": \"public\",\n"
                + "            \"site_admin\": false\n"
                + "        },\n"
                + "        \"parents\": [\n"
                + "            {\n"
                + "                \"sha\": \"2c3b2d336f11d375430257bf9a89206efab79ba2\",\n"
                + "                \"url\": \"https://api.github.com/repos/nifreebie/Link_tracker_test/commits/2c3b2d336f11d375430257bf9a89206efab79ba2\",\n"
                + "                \"html_url\": \"https://github.com/nifreebie/Link_tracker_test/commit/2c3b2d336f11d375430257bf9a89206efab79ba2\"\n"
                + "            }\n"
                + "        ]\n"
                + "    }]";
        wireMockServer.stubFor(get(urlMatching("/repos/.*/.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(jsonResponse)));

        Mono<EventDTO> response = gitHubClient.getRepoLastUpdated("owner", "repo");
        LocalDateTime expectedDate = LocalDateTime.of(2024, 3, 1, 12, 0, 0);

        EventDTO expected = new EventDTO(null, null, expectedDate, null, null);

        StepVerifier.create(response)
                .expectNextMatches(event -> expected.date().equals(event.date()))
                .verifyComplete();
    }

    @Test
    void testGetLastIssueCreatedWithValidResponse() {
        String jsonResponse = "[{" + "\"title\": \"Bug in login feature\","
                + "\"user\": {\"login\": \"octocat\"},"
                + "\"created_at\": \"2024-03-01T10:00:00Z\","
                + "\"body\": \"Login fails when using special characters.\","
                + "\"number\": 42"
                + "}]";

        wireMockServer.stubFor(get(urlMatching("/repos/.*/.*/issues.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(jsonResponse)));

        LocalDateTime expectedDate = LocalDateTime.of(2024, 3, 1, 10, 0);

        EventDTO expected = new EventDTO(
                "Bug in login feature",
                "octocat",
                expectedDate,
                "Login fails when using special characters.",
                EventType.ISSUE);

        Mono<EventDTO> response = gitHubClient.getLastIssueCreated("owner", "repo");

        StepVerifier.create(response)
                .expectNextMatches(event -> expected.title().equals(event.title())
                        && expected.username().equals(event.username())
                        && expected.date().equals(event.date())
                        && expected.description().equals(event.description())
                        && expected.eventType() == event.eventType())
                .verifyComplete();
    }

    @Test
    void testGetLastIssueCreatedWithNoIssues() {
        wireMockServer.stubFor(get(urlMatching("/repos/.*/.*/issues.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[]")));

        Mono<EventDTO> response = gitHubClient.getLastIssueCreated("owner", "repo");

        StepVerifier.create(response)
                .expectNextMatches(
                        event -> "No new issues".equals(event.title()) && event.eventType() == EventType.NO_CHANGES)
                .verifyComplete();
    }

    @Test
    void testGetLastIssueCreatedWithNotFound() {
        wireMockServer.stubFor(get(urlMatching("/repos/.*/.*/issues.*"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"message\": \"Not Found\"}")));

        Mono<EventDTO> response = gitHubClient.getLastIssueCreated("owner", "repo");

        StepVerifier.create(response)
                .expectErrorMatches(
                        throwable -> throwable.getMessage().toLowerCase().contains("not found"))
                .verify();
    }
}
