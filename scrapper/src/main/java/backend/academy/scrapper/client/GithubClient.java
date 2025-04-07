package backend.academy.scrapper.client;

import backend.academy.scrapper.model.dto.EventDTO;
import reactor.core.publisher.Mono;

public interface GithubClient {
    Mono<EventDTO> getRepoLastUpdated(String owner, String repo);

    Mono<EventDTO> getLastIssueCreated(String owner, String repo);
}
