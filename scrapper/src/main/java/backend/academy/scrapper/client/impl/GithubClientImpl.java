package backend.academy.scrapper.client.impl;

import backend.academy.scrapper.ScrapperConfig;
import backend.academy.scrapper.client.GithubClient;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class GithubClientImpl implements GithubClient {
    private final WebClient webClient;

    private final ScrapperConfig config;

    private static final String GITHUB_API_URL = "https://api.github.com";

    @Autowired
    public GithubClientImpl(ScrapperConfig config) {
        this(GITHUB_API_URL, config);
    }

    public GithubClientImpl(String url, ScrapperConfig config) {
        this.config = config;
        this.webClient = WebClient.builder().baseUrl(url).build();
    }

    @Override
    public Mono<String> getRepoLastUpdated(String owner, String repo) {
        log.info("Fetching last updated timestamp for repo {}/{}", owner, repo);
        return webClient
                .get()
                .uri("/repos/{owner}/{repo}", owner, repo)
                .header("Authorization", "token " + config.githubToken())
                .header("Accept", "application/vnd.github.v3+json")
                .retrieve()
                .onStatus(HttpStatus.BAD_REQUEST::equals, response -> {
                    log.warn("Bad request for repo {}/{}", owner, repo);
                    return response.bodyToMono(String.class)
                            .flatMap(errorBody -> Mono.error(new RuntimeException("Bad request: " + errorBody)));
                })
                .onStatus(HttpStatus.NOT_FOUND::equals, response -> {
                    log.warn("Repo not found: {}/{}", owner, repo);
                    return response.bodyToMono(String.class)
                            .flatMap(errorBody -> Mono.error(new RuntimeException("Not found: " + errorBody)));
                })
                .onStatus(HttpStatus.INTERNAL_SERVER_ERROR::equals, response -> {
                    log.error("Internal server error while fetching repo {}/{}", owner, repo);
                    return response.bodyToMono(String.class)
                            .flatMap(errorBody ->
                                    Mono.error(new RuntimeException("Internal server error: " + errorBody)));
                })
                .bodyToMono(Map.class)
                .map(response -> (String) response.get("updated_at"))
                .doOnSuccess(updatedAt -> log.info("Repo {}/{} last updated at: {}", owner, repo, updatedAt))
                .doOnError(error -> log.error(
                        "Error fetching last updated timestamp for repo {}/{} - {}", owner, repo, error.getMessage()));
    }
}
