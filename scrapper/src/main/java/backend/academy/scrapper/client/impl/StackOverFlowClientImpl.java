package backend.academy.scrapper.client.impl;

import backend.academy.scrapper.client.StackOverFlowClient;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class StackOverFlowClientImpl implements StackOverFlowClient {
    private final WebClient webClient;

    @Value(value = "${app.stackoverflow.key}")
    private String key;

    @Value("${app.stackoverflow.access-token}")
    private String accessToken;

    private static final String STACKOVERFLOW_API_URL = "https://api.stackexchange.com/2.3";

    public StackOverFlowClientImpl(String url) {
        this.webClient = WebClient.builder().baseUrl(url).build();
    }

    public StackOverFlowClientImpl() {
        this.webClient = WebClient.builder().baseUrl(STACKOVERFLOW_API_URL).build();
    }

    @Override
    public Mono<String> getQuestionLastUpdated(String id) {
        log.info("Fetching last activity date for StackOverflow question: {}", id);
        return webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/questions/{id}")
                        .queryParam("order", "desc")
                        .queryParam("sort", "activity")
                        .queryParam("site", "stackoverflow")
                        .queryParam("key", key)
                        .queryParam("access_token", accessToken)
                        .build(id))
                .retrieve()
                .onStatus(HttpStatus.BAD_REQUEST::equals, response -> {
                    log.warn("Bad request for question ID: {}", id);
                    return response.bodyToMono(String.class)
                            .flatMap(errorBody -> Mono.error(new RuntimeException("Bad request: " + errorBody)));
                })
                .onStatus(HttpStatus.NOT_FOUND::equals, response -> {
                    log.warn("Question not found: {}", id);
                    return response.bodyToMono(String.class)
                            .flatMap(errorBody -> Mono.error(new RuntimeException("Not found: " + errorBody)));
                })
                .onStatus(HttpStatus.INTERNAL_SERVER_ERROR::equals, response -> {
                    log.error("Internal server error while fetching question: {}", id);
                    return response.bodyToMono(String.class)
                            .flatMap(errorBody ->
                                    Mono.error(new RuntimeException("Internal server error: " + errorBody)));
                })
                .bodyToMono(Map.class)
                .map(response -> {
                    List<?> items = (List<?>) response.get("items");
                    Map<String, ?> map = (Map<String, ?>) items.getFirst();
                    return map.get("last_activity_date").toString();
                })
                .doOnSuccess(lastUpdated -> log.info("Question {} last updated at: {}", id, lastUpdated))
                .doOnError(error ->
                        log.error("Error fetching last activity date for question {} - {}", id, error.getMessage()));
    }
}
