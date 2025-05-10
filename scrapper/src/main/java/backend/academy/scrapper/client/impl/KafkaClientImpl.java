package backend.academy.scrapper.client.impl;

import backend.academy.scrapper.ScrapperConfig;
import backend.academy.scrapper.client.BotClient;
import backend.academy.scrapper.model.dto.EventDTO;
import backend.academy.scrapper.model.dto.LinkDTO;
import backend.academy.scrapper.model.dto.request.LinkUpdateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaClientImpl implements BotClient {
    private final KafkaTemplate<String, LinkUpdateRequest> kafkaTemplate;
    private final ScrapperConfig scrapperConfig;

    @Autowired
    public KafkaClientImpl(KafkaTemplate<String, LinkUpdateRequest> kafkaTemplate, ScrapperConfig scrapperConfig) {
        this.kafkaTemplate = kafkaTemplate;
        this.scrapperConfig = scrapperConfig;
    }

    @Override
    public void update(LinkDTO link, EventDTO eventDTO) {
        LinkUpdateRequest request = new LinkUpdateRequest(
                link.id(),
                link.url(),
                eventDTO.description(),
                link.telegramChatIds(),
                eventDTO.title(),
                eventDTO.username(),
                eventDTO.date(),
                eventDTO.eventType());
        kafkaTemplate.send(scrapperConfig.topics().updates(), request);
    }
}
