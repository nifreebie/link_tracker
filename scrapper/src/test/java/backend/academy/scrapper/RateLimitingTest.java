package backend.academy.scrapper;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import backend.academy.scrapper.util.LiquibaseMigration;
import java.nio.file.Paths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({TestcontainersConfiguration.class})
public class RateLimitingTest {
    @LocalServerPort
    private int port;

    @Value("${resilience4j.ratelimiter.configs.default.limitForPeriod}")
    private int limit;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PostgreSQLContainer<?> postgresContainer;

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
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
    }

    private String getUrl() {
        return "http://localhost:" + port + "/api/v1/tg-chat";
    }

    @Test
    void shouldReturn429WhenRateLimitExceeded() {
        int extra = 3;

        int successful = 0;
        int rateLimited = 0;

        for (int i = 0; i < limit + extra; i++) {
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(getUrl(), HttpMethod.GET, entity, String.class);

            HttpStatusCode status = response.getStatusCode();
            if (status.is2xxSuccessful()) {
                successful++;
            } else if (status == HttpStatus.TOO_MANY_REQUESTS) {
                rateLimited++;
            }
        }
        assertThat(successful).isEqualTo(limit);
        assertThat(rateLimited).isEqualTo(extra);
    }
}
