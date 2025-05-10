package backend.academy.scrapper.client.impl;

import backend.academy.scrapper.ScrapperConfig;
import backend.academy.scrapper.client.GithubClient;
import backend.academy.scrapper.model.domain.EventType;
import backend.academy.scrapper.model.dto.EventDTO;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class GithubClientImpl implements GithubClient {
    private final WebClient webClient;

    private final ScrapperConfig config;

    @Autowired
    public GithubClientImpl(
            @Value("${app.github-api-url}") String url, ScrapperConfig config, WebClient.Builder webClientBuilder) {
        this.config = config;
        this.webClient = webClientBuilder.baseUrl(url).build();
    }

    @Override
    public Mono<EventDTO> getRepoLastUpdated(String owner, String repo) {
        log.info("Fetching last commit for repo {}/{}", owner, repo);
        return webClient
                .get()
                .uri(uriBuilder ->
                        uriBuilder.path("/repos/{owner}/{repo}/commits").build(owner, repo))
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
                .bodyToFlux(Map.class)
                .next()
                .map(commit -> {
                    Map commitDetails = (Map) commit.get("commit");
                    String message = (String) commitDetails.get("message");
                    String title = message != null ? message.split("\n")[0] : "";

                    Map authorDetails = (Map) commitDetails.get("author");
                    String dateStr = authorDetails != null ? (String) authorDetails.get("date") : null;
                    LocalDateTime date =
                            dateStr != null ? OffsetDateTime.parse(dateStr).toLocalDateTime() : null;

                    Map topLevelAuthor = (Map) commit.get("author");
                    String username = topLevelAuthor != null
                            ? (String) topLevelAuthor.get("login")
                            : authorDetails != null ? (String) authorDetails.get("name") : "";

                    return new EventDTO(title, username, date, "", EventType.COMMIT);
                })
                .doOnSuccess(commitDetails -> log.info("Repo {}/{} last commit: {}", owner, repo, commitDetails))
                .doOnError(error ->
                        log.error("Error fetching last commit for repo {}/{} - {}", owner, repo, error.getMessage()));
    }

    @Override
    public Mono<EventDTO> getLastIssueCreated(String owner, String repo) {
        log.info("Fetching last event for repo {}/{}", owner, repo);
        return webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/repos/{owner}/{repo}/issues")
                        .queryParam("state", "all")
                        .queryParam("sort", "created")
                        .queryParam("direction", "desc")
                        .build(owner, repo))
                .header("Authorization", "token " + config.githubToken())
                .header("Accept", "application/vnd.github.v3+json")
                .retrieve()
                .bodyToFlux(Map.class)
                .next()
                .map(issue -> {
                    String title = (String) issue.get("title");
                    Map user = (Map) issue.get("user");
                    String username = user != null ? (String) user.get("login") : "";
                    String createdAt = (String) issue.get("created_at");
                    LocalDateTime date = OffsetDateTime.parse(createdAt).toLocalDateTime();
                    String body = (String) issue.get("body");
                    String description = (body != null && body.length() > 200) ? body.substring(0, 200) : body;
                    EventType changeType = issue.containsKey("pull_request") ? EventType.PR : EventType.ISSUE;
                    return new EventDTO(title, username, date, description, changeType);
                })
                .switchIfEmpty(Mono.just(new EventDTO("No new issues", "", null, "", EventType.NO_CHANGES)))
                .doOnSuccess(commitDetails -> log.info("Repo {}/{} last event: {}", owner, repo, commitDetails))
                .doOnError(error ->
                        log.error("Error fetching last event for repo {}/{} - {}", owner, repo, error.getMessage()));
    }
}
