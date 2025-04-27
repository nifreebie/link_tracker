package backend.academy.scrapper;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app", ignoreUnknownFields = false)
public record ScrapperConfig(
        @NotEmpty String githubToken,
        StackOverflowCredentials stackOverflow,
        @NotEmpty String accessType,
        Integer batchSize,
        String messageTransport,
        Topics topics) {
    public record StackOverflowCredentials(@NotEmpty String key, @NotEmpty String accessToken) {}

    public record Topics(String updates, String dlq) {}
}
