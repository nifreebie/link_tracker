package backend.academy.scrapper.client;

import reactor.core.publisher.Mono;

public interface GithubClient {
    Mono<String> getRepoLastUpdated(String owner, String repo);
}
