package backend.academy.scrapper.client.impl;

import backend.academy.scrapper.client.BotClient;
import backend.academy.scrapper.model.dto.EventDTO;
import backend.academy.scrapper.model.dto.LinkDTO;
import backend.academy.scrapper.model.dto.request.LinkUpdateRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class BotClientImpl implements BotClient {
    private final WebClient webClient;
    private static final String BOT_API_URL = "http://localhost:8080/api/v1";

    public BotClientImpl(String url) {
        this.webClient = WebClient.builder().baseUrl(url).build();
    }

    public BotClientImpl() {
        this.webClient = WebClient.builder().baseUrl(BOT_API_URL).build();
    }

    @Override
    public void update(LinkDTO link, EventDTO eventDTO) {
        log.info("Updating link: {} for chat IDs: {}", link.url(), link.telegramChatIds());
        LinkUpdateRequest request = new LinkUpdateRequest(
                link.id(),
                link.url(),
                eventDTO.description(),
                link.telegramChatIds(),
                eventDTO.title(),
                eventDTO.username(),
                eventDTO.date(),
                eventDTO.eventType());
        webClient
                .post()
                .uri("/updates")
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatus.CONFLICT::equals, response -> {
                    log.warn("Conflict error while updating link: {}", link.url());
                    return response.bodyToMono(String.class)
                            .flatMap(errorBody -> Mono.error(new RuntimeException("Conflict error: " + errorBody)));
                })
                .onStatus(HttpStatus.BAD_REQUEST::equals, response -> {
                    log.warn("Bad request while updating link: {}", link.url());
                    return response.bodyToMono(String.class)
                            .flatMap(errorBody -> Mono.error(new RuntimeException("Bad request: " + errorBody)));
                })
                .onStatus(HttpStatus.INTERNAL_SERVER_ERROR::equals, response -> {
                    log.error("Internal server error while updating link: {}", link.url());
                    return response.bodyToMono(String.class)
                            .flatMap(errorBody ->
                                    Mono.error(new RuntimeException("Internal server error: " + errorBody)));
                })
                .bodyToMono(String.class)
                .doOnSuccess(response -> log.info("Successfully updated link: {}. Response: {}", link.url(), response))
                .doOnError(error -> log.error("Error updating link: {} - {}", link.url(), error.getMessage()))
                .subscribe();
    }
}
