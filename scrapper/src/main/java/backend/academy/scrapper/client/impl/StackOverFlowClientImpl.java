package backend.academy.scrapper.client.impl;

import backend.academy.scrapper.ScrapperConfig;
import backend.academy.scrapper.client.StackOverFlowClient;
import backend.academy.scrapper.model.domain.EventType;
import backend.academy.scrapper.model.dto.EventDTO;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Component
public class StackOverFlowClientImpl implements StackOverFlowClient {
    private final WebClient webClient;

    private final ScrapperConfig config;

    @Autowired
    public StackOverFlowClientImpl(
            @Value("${app.stackoverflow-api-url}") String url,
            ScrapperConfig config,
            WebClient.Builder webClientBuilder) {
        this.config = config;
        this.webClient = webClientBuilder.baseUrl(url).build();
    }

    @Override
    // CPD-OFF
    public Mono<EventDTO> getQuestionLastAnswer(String id) {
        log.info("Fetching last answer for question ID: {}", id);

        return webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/questions/{id}/answers")
                        .queryParam("order", "desc")
                        .queryParam("sort", "creation")
                        .queryParam("site", "stackoverflow")
                        .queryParam("filter", "withbody")
                        .queryParam("key", config.stackOverflow().key())
                        .queryParam("access_token", config.stackOverflow().accessToken())
                        .build(id))
                .retrieve()
                .bodyToMono(Map.class)
                .publishOn(Schedulers.boundedElastic())
                .mapNotNull(map -> {
                    EventDTO eventDTO = mapToEventDTO(map, EventType.ANSWER);
                    log.info("Extracted last answer event: {}", eventDTO);
                    return eventDTO;
                })
                .doOnSuccess(event -> log.info("Successfully retrieved last answer for question ID {}: {}", id, event))
                .doOnError(error ->
                        log.error("Error fetching last answer for question ID {}: {}", id, error.getMessage()));
    }

    @Override
    public Mono<EventDTO> getQuestionLastComment(String id) {
        log.info("Fetching last comment for question ID: {}", id);

        return webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/questions/{id}/comments")
                        .queryParam("order", "desc")
                        .queryParam("sort", "creation")
                        .queryParam("site", "stackoverflow")
                        .queryParam("filter", "withbody")
                        .queryParam("key", config.stackOverflow().key())
                        .queryParam("access_token", config.stackOverflow().accessToken())
                        .build(id))
                .retrieve()
                .bodyToMono(Map.class)
                .publishOn(Schedulers.boundedElastic())
                .mapNotNull(map -> {
                    EventDTO eventDTO = mapToEventDTO(map, EventType.COMMENT);
                    log.info("Extracted last comment event: {}", eventDTO);
                    return eventDTO;
                })
                .doOnSuccess(event -> log.info("Successfully retrieved last comment for question ID {}: {}", id, event))
                .doOnError(error ->
                        log.error("Error fetching last comment for question ID {}: {}", id, error.getMessage()));
    }

    private Mono<EventDTO> extractEvent(Map<String, Object> map, EventType eventType) {
        List<Map<String, Object>> items = (List<Map<String, Object>>) map.get("items");

        if (items == null || items.isEmpty()) {
            return Mono.empty();
        }

        Map<String, Object> item = items.getFirst();

        String title = (String) item.getOrDefault("title", "Без заголовка");
        String body = (String) item.getOrDefault("body", "");
        long creationDate = ((Number) item.getOrDefault("creation_date", 0)).longValue();
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(creationDate), ZoneOffset.UTC);

        Map<String, Object> owner = (Map<String, Object>) item.get("owner");
        String username = owner != null ? (String) owner.getOrDefault("display_name", "Аноним") : "Аноним";

        String preview = body.length() > 200 ? body.substring(0, 200) : body;

        return Mono.just(new EventDTO(title, username, dateTime, preview, eventType));
    }

    private EventDTO mapToEventDTO(Map<String, Object> map, EventType eventType) {
        return (EventDTO) extractEvent(map, eventType).block();
    }
}
