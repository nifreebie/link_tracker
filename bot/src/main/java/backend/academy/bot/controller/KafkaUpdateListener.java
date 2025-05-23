package backend.academy.bot.controller;

import backend.academy.bot.model.dto.request.LinkUpdateRequest;
import backend.academy.bot.service.UpdateService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaUpdateListener {
    private final UpdateService updateService;

    @KafkaListener(
            topics = "${app.topics.updates}",
            groupId = "bot-group",
            containerFactory = "linkUpdateKafkaListenerContainerFactory")
    public void listen(LinkUpdateRequest request) {
        updateService.sendUpdates(request);
    }
}
