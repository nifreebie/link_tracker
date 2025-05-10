package backend.academy.scrapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import backend.academy.scrapper.client.impl.BotClientImpl;
import backend.academy.scrapper.client.impl.KafkaClientImpl;
import backend.academy.scrapper.client.impl.SecureBotClient;
import backend.academy.scrapper.model.domain.EventType;
import backend.academy.scrapper.model.domain.LinkType;
import backend.academy.scrapper.model.dto.EventDTO;
import backend.academy.scrapper.model.dto.LinkDTO;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = "app.message-transport=Kafka")
public class FallbackClientTest {
    private KafkaClientImpl kafkaClient;
    private BotClientImpl httpClient;
    private LinkDTO link;
    private EventDTO event;

    @Autowired
    private ScrapperConfig config;

    @BeforeEach
    void setUp() {
        kafkaClient = mock(KafkaClientImpl.class);
        httpClient = mock(BotClientImpl.class);

        link = new LinkDTO(
                1,
                "https://github.com/nifreebie/Link_tracker_test",
                List.of("tag"),
                List.of("filter"),
                LocalDateTime.now(),
                List.of(1L),
                LinkType.GITHUB);
        event = new EventDTO("commit", "user", LocalDateTime.now(), "", EventType.COMMIT);
    }

    @Test
    void testPrimarySuccess() {
        SecureBotClient secureBotClient = new SecureBotClient(kafkaClient, httpClient, config);

        secureBotClient.update(link, event);

        verify(kafkaClient, times(1)).update(link, event);
        verify(httpClient, never()).update(any(), any());
    }

    @Test
    void testPrimaryFailsSecondarySuccess() {
        doThrow(new RuntimeException("Kafka fail")).when(kafkaClient).update(link, event);
        SecureBotClient fallbackBotClient = new SecureBotClient(kafkaClient, httpClient, config);

        fallbackBotClient.update(link, event);

        verify(kafkaClient, times(1)).update(link, event);
        verify(httpClient, times(1)).update(link, event);
    }

    @Test
    void testBothFailThrows() {
        doThrow(new RuntimeException("Kafka fail")).when(kafkaClient).update(link, event);
        doThrow(new RuntimeException("HTTP fail")).when(httpClient).update(link, event);

        SecureBotClient fallbackBotClient = new SecureBotClient(kafkaClient, httpClient, config);

        org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class, () -> fallbackBotClient.update(link, event));

        verify(kafkaClient, times(1)).update(link, event);
        verify(httpClient, times(1)).update(link, event);
    }
}
