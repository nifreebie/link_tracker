package backend.academy.bot;

import jakarta.validation.constraints.NotEmpty;
import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app", ignoreUnknownFields = false)
public record BotConfig(
        String scrapperApiUrl,
        @NotEmpty String telegramToken,
        Topics topics,
        int cacheTTL,
        Duration responseTimeout,
        Duration connectionTimeout,
        Integer maxRetries,
        Duration backoff,
        List<Integer> retryStatusCodes) {
    public record Topics(String updates, String dlq) {}
}
