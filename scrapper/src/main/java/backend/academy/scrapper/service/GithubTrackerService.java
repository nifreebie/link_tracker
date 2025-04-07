package backend.academy.scrapper.service;

import backend.academy.scrapper.model.dto.EventDTO;
import reactor.core.publisher.Mono;

public interface GithubTrackerService {
    Mono<EventDTO> trackCommit(String url);

    Mono<EventDTO> trackEvent(String url);
}
