package backend.academy.bot;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.model.command.impl.ListCommand;
import backend.academy.bot.model.command.impl.TrackCommand;
import backend.academy.bot.model.command.impl.UntrackCommand;
import backend.academy.bot.model.dto.response.LinkResponse;
import backend.academy.bot.model.dto.response.ListLinksResponse;
import backend.academy.bot.repository.StateRepository;
import backend.academy.bot.service.commandHandler.impl.ListCommandHandler;
import backend.academy.bot.service.commandHandler.impl.TrackCommandHandler;
import backend.academy.bot.service.commandHandler.impl.UntrackCommandHandler;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.core.publisher.Mono;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true")
@Testcontainers
public class RedisCacheTest {
    @Container
    static GenericContainer<?> redis =
        new GenericContainer<>(DockerImageName.parse("redis:7-alpine")).withExposedPorts(6379);

    @TestConfiguration
    static class RedisTestConfig {
        @Bean
        public LettuceConnectionFactory redisConnectionFactory() {
            RedisStandaloneConfiguration cfg = new RedisStandaloneConfiguration(
                redis.getHost(),
                redis.getMappedPort(6379)
            );
            return new LettuceConnectionFactory(cfg);
        }

        @Bean
        public StringRedisTemplate stringRedisTemplate(LettuceConnectionFactory cf) {
            return new StringRedisTemplate(cf);
        }
    }

    @Autowired
    private ListCommandHandler listCommandHandler;

    @Autowired
    private TrackCommandHandler trackCommandHandler;

    @Autowired
    private UntrackCommandHandler untrackCommandHandler;

    @Autowired
    private ScrapperClient scrapperClient;

    private final long chatId = 12345L;

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public ScrapperClient scrapperClient() {
            return Mockito.mock(ScrapperClient.class);
        }

        @Bean
        @Primary
        public StateRepository stateRepository() {
            return Mockito.mock(StateRepository.class);
        }
    }

    @BeforeEach
    void setUp() {
        reset(scrapperClient);

        LinkResponse response = new LinkResponse(1, "https://example.com", List.of(), List.of());
        ListLinksResponse listLinksResponse = new ListLinksResponse(List.of(response), 1);

        when(scrapperClient.getUserLinks(anyLong())).thenReturn(Mono.just(listLinksResponse));
        when(scrapperClient.track(anyLong(), anyString(), anyList(), anyList()))
            .thenReturn(Mono.just("Ccылка отслеживаеется"));
        when(scrapperClient.untrack(anyLong(), anyString())).thenReturn(Mono.just("Ссылка больше не отслеживается"));
    }

    @Test
    void testCacheUsageAndInvalidation() {
        ListCommand listCommand = new ListCommand(chatId);
        String firstResult = listCommandHandler.handle(listCommand);

        verify(scrapperClient, times(1)).getUserLinks(chatId);

        String cachedResult = listCommandHandler.handle(listCommand);
        verify(scrapperClient, times(1)).getUserLinks(chatId);

        assertThat(cachedResult).isEqualTo(firstResult);

        TrackCommand trackCommand = new TrackCommand(chatId, "https://example.com", List.of(), List.of());
        String trackResult = trackCommandHandler.handle(trackCommand);

        assertThat(trackResult).isEqualTo("Ccылка отслеживаеется");

        listCommandHandler.handle(listCommand);
        verify(scrapperClient, times(2)).getUserLinks(chatId);

        UntrackCommand untrackCommand = new UntrackCommand(chatId, "https://example.com");
        String untrackResult = untrackCommandHandler.handle(untrackCommand);

        assertThat(untrackResult).isEqualTo("Ссылка больше не отслеживается");

        listCommandHandler.handle(listCommand);
        verify(scrapperClient, times(3)).getUserLinks(chatId);
    }
}
