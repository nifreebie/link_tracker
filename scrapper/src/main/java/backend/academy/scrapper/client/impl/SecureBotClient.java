package backend.academy.scrapper.client.impl;

import backend.academy.scrapper.ScrapperConfig;
import backend.academy.scrapper.client.BotClient;
import backend.academy.scrapper.model.dto.EventDTO;
import backend.academy.scrapper.model.dto.LinkDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Primary
public class SecureBotClient implements BotClient {
    private final BotClient primary;
    private final BotClient secondary;

    public SecureBotClient(
            @Qualifier("kafkaClientImpl") BotClient kafkaClientImpl,
            @Qualifier("botClientImpl") BotClient httpClientImpl,
            ScrapperConfig config) {
        if ("Kafka".equalsIgnoreCase(config.messageTransport())) {
            this.primary = kafkaClientImpl;
            this.secondary = httpClientImpl;
        } else {
            this.primary = httpClientImpl;
            this.secondary = kafkaClientImpl;
        }
    }

    @Override
    public void update(LinkDTO link, EventDTO eventDTO) {
        try {
            log.info("Using primary transport: {}", primary.getClass().getSimpleName());
            primary.update(link, eventDTO);
        } catch (Exception ex) {
            log.warn(
                    "Primary transport {} failed. Fallback to {}",
                    primary.getClass().getSimpleName(),
                    secondary.getClass().getSimpleName(),
                    ex);
            try {
                secondary.update(link, eventDTO);
            } catch (Exception ex2) {
                log.error(
                        "Secondary transport {} also failed. Giving up.",
                        secondary.getClass().getSimpleName(),
                        ex2);
                throw ex2;
            }
        }
    }
}
