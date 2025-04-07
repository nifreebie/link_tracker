package backend.academy.bot.client.impl;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.exceptions.IsAlreadyRegisteredException;
import backend.academy.bot.exceptions.NotFoundException;
import backend.academy.bot.model.dto.request.AddLinkRequest;
import backend.academy.bot.model.dto.request.ChangeLinkTagsRequest;
import backend.academy.bot.model.dto.request.RemoveLinkRequest;
import backend.academy.bot.model.dto.response.ApiErrorResponse;
import backend.academy.bot.model.dto.response.LinkResponse;
import backend.academy.bot.model.dto.response.ListLinksResponse;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
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

    @Override
    public Mono<String> createTag(Long id, String tagName) {
        return webClient
                .post()
                .uri("/tags")
                .header("Tg-Chat-Id", id.toString())
                .bodyValue(tagName)
                .retrieve()
                .onStatus(HttpStatus.CONFLICT::equals, response -> {
                    log.warn("Tag already being created for user {}: {}", id, tagName);
                    return response.bodyToMono(ApiErrorResponse.class)
                            .map(ApiErrorResponse::exceptionMessage)
                            .flatMap(msg -> Mono.error(new IsAlreadyRegisteredException(msg)));
                })
                .bodyToMono(String.class)
                .map(response -> {
                    log.info("Successfully created tag for user {}: {}", id, response);
                    return "Тэг " + response + " успешно создан";
                })
                .onErrorResume(IsAlreadyRegisteredException.class, ex -> {
                    log.error("Error creating tag for user {}: {} - {}", id, tagName, ex.getMessage());
                    return Mono.just(ex.getMessage());
                });
    }

    @Override
    public Mono<List<String>> getUserTags(Long id) {
        log.info("Requesting tags for user with id: {}", id);
        return webClient
                .get()
                .uri("/tags")
                .header("Tg-Chat-Id", id.toString())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<String>>() {})
                .doOnSuccess(tags -> log.info("Successfully retrieved tags for user {}: {}", id, tags))
                .doOnError(error -> log.error("Error fetching tags for user {}: {}", id, error.getMessage()));
    }

    @Override
    public Mono<List<Long>> getAllUsers() {
        log.info("Requesting the list of all users");
        return webClient
                .get()
                .uri("/tg-chat")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Long>>() {})
                .doOnSuccess(users -> log.info("Successfully retrieved user list: {}", users))
                .doOnError(error -> log.error("Error fetching user list: {}", error.getMessage()));
    }

    @Override
    public Mono<String> addTags(Long id, String url, List<String> tags) {
        ChangeLinkTagsRequest request = new ChangeLinkTagsRequest(url, tags);
        log.info("Adding tags {} for user {} and URL: {}", tags, id, url);
        return webClient
                .post()
                .uri("/links/tags")
                .header("Tg-Chat-Id", id.toString())
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatus.CONFLICT::equals, response -> response.bodyToMono(ApiErrorResponse.class)
                        .flatMap(apiError -> {
                            log.error("Conflict error while adding tags: {}", apiError.exceptionMessage());
                            return Mono.error(new IsAlreadyRegisteredException(apiError.exceptionMessage()));
                        }))
                .onStatus(HttpStatus.NOT_FOUND::equals, response -> response.bodyToMono(ApiErrorResponse.class)
                        .flatMap(apiError -> {
                            log.error("Not found error while adding tags: {}", apiError.exceptionMessage());
                            return Mono.error(new IsAlreadyRegisteredException(apiError.exceptionMessage()));
                        }))
                .bodyToMono(String.class)
                .doOnSuccess(response -> log.info("Successfully added tags for URL {}: {}", url, response))
                .doOnError(error -> log.error("Error adding tags for URL {}: {}", url, error.getMessage()))
                .onErrorResume(IsAlreadyRegisteredException.class, ex -> Mono.just(ex.getMessage()))
                .onErrorResume(NotFoundException.class, ex -> Mono.just(ex.getMessage()));
    }

    @Override
    public Mono<String> removeTags(Long id, String url, List<String> tags) {
        ChangeLinkTagsRequest request = new ChangeLinkTagsRequest(url, tags);
        log.info("Removing tags {} for user {} and URL: {}", tags, id, url);
        return webClient
                .method(org.springframework.http.HttpMethod.DELETE)
                .uri("/links/tags")
                .header("Tg-Chat-Id", id.toString())
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatus.NOT_FOUND::equals, response -> response.bodyToMono(ApiErrorResponse.class)
                        .flatMap(apiError -> {
                            log.error("Not found error while removing tags: {}", apiError.exceptionMessage());
                            return Mono.error(new NotFoundException(apiError.exceptionMessage()));
                        }))
                .bodyToMono(String.class)
                .doOnSuccess(response -> log.info("Successfully removed tags for URL {}: {}", url, response))
                .doOnError(error -> log.error("Error removing tags for URL {}: {}", url, error.getMessage()))
                .onErrorResume(NotFoundException.class, ex -> Mono.just(ex.getMessage()));
    }
}
