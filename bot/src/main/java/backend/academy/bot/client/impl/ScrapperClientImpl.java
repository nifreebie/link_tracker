package backend.academy.bot.client.impl;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.dto.request.AddLinkRequest;
import backend.academy.bot.dto.request.RemoveLinkRequest;
import backend.academy.bot.dto.response.ApiErrorResponse;
import backend.academy.bot.dto.response.LinkResponse;
import backend.academy.bot.dto.response.ListLinksResponse;
import backend.academy.bot.exceptions.IsAlreadyRegisteredException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class ScrapperClientImpl implements ScrapperClient {
    private final WebClient webClient;

    private static final String SCRAPPER_API_URL = "http://localhost:8081/api/v1";

    public ScrapperClientImpl(String url) {
        this.webClient = WebClient.builder().baseUrl(url).build();
    }

    public ScrapperClientImpl() {
        this.webClient = WebClient.builder().baseUrl(SCRAPPER_API_URL).build();
    }

    @Override
    public Mono<String> register(Long id) {
        log.info("Attempting to register user with id: {}", id);
        return webClient
            .post()
            .uri("/tg-chat/{id}", id)
            .retrieve()
            .onStatus(HttpStatus.CONFLICT::equals, response -> {
                log.warn("User with id {} is already registered", id);
                return response.bodyToMono(ApiErrorResponse.class)
                    .map(ApiErrorResponse::exceptionMessage)
                    .flatMap(msg -> Mono.error(new IsAlreadyRegisteredException(msg)));
            })
            .bodyToMono(String.class)
            .onErrorResume(IsAlreadyRegisteredException.class, ex -> {
                log.error("Error registering user {}: {}", id, ex.getMessage());
                return Mono.just(ex.getMessage());
            });
    }

    public Mono<ListLinksResponse> getUserLinks(Long id) {
        log.info("Fetching links for user with id: {}", id);
        return webClient
            .get()
            .uri("/links")
            .header("Tg-Chat-Id", id.toString())
            .retrieve()
            .bodyToMono(ListLinksResponse.class);
    }

    public Mono<String> track(Long id, String url, List<String> tags, List<String> filters) {
        log.info("Tracking new link for user {}: {}", id, url);
        AddLinkRequest request = new AddLinkRequest(url, tags, filters);
        return webClient
            .post()
            .uri("/links")
            .header("Tg-Chat-Id", id.toString())
            .bodyValue(request)
            .retrieve()
            .onStatus(HttpStatus.CONFLICT::equals, response -> {
                log.warn("Link already being tracked for user {}: {}", id, url);
                return response.bodyToMono(ApiErrorResponse.class)
                    .map(ApiErrorResponse::exceptionMessage)
                    .flatMap(msg -> Mono.error(new IsAlreadyRegisteredException(msg)));
            })
            .bodyToMono(LinkResponse.class)
            .map(response -> {
                log.info("Successfully added link for user {}: {}", id, url);
                return "Сcылка успешно добавлена";
            })
            .onErrorResume(IsAlreadyRegisteredException.class, ex -> {
                log.error("Error tracking link for user {}: {} - {}", id, url, ex.getMessage());
                return Mono.just(ex.getMessage());
            });
    }

    @Override
    public Mono<String> untrack(Long id, String url) {
        log.info("Untracking link for user {}: {}", id, url);
        RemoveLinkRequest request = new RemoveLinkRequest(url);
        return webClient
            .method(org.springframework.http.HttpMethod.DELETE)
            .uri("/links")
            .header("Tg-Chat-Id", id.toString())
            .bodyValue(request)
            .retrieve()
            .onStatus(HttpStatus.NOT_FOUND::equals, response -> {
                log.warn("Attempted to untrack a non-existing link for user {}: {}", id, url);
                return response.bodyToMono(ApiErrorResponse.class)
                    .map(ApiErrorResponse::exceptionMessage)
                    .flatMap(msg -> Mono.error(new IsAlreadyRegisteredException(msg)));
            })
            .bodyToMono(LinkResponse.class)
            .map(response -> {
                log.info("Successfully removed link for user {}: {}", id, url);
                return "Сcылка успешно удалена";
            })
            .onErrorResume(IsAlreadyRegisteredException.class, ex -> {
                log.error("Error untracking link for user {}: {} - {}", id, url, ex.getMessage());
                return Mono.just(ex.getMessage());
            });
    }
}
