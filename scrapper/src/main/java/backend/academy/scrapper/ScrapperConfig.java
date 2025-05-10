package backend.academy.scrapper;

import jakarta.validation.constraints.NotEmpty;
import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app", ignoreUnknownFields = false)
public record ScrapperConfig(
        String botApiUrl,
        String githubApiUrl,
        String stackOverflowApiUrl,
        @NotEmpty String githubToken,
        StackOverflowCredentials stackOverflow,
        @NotEmpty String accessType,
        Integer batchSize,
        String messageTransport,
        Topics topics,
        Duration responseTimeout,
        Duration connectionTimeout,
        Integer maxRetries,
        Duration backoff,
        List<Integer> retryStatusCodes) {
    public record StackOverflowCredentials(@NotEmpty String key, @NotEmpty String accessToken) {}

    public record Topics(String updates, String dlq) {}
}
