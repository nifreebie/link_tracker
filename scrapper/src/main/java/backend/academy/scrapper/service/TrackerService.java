package backend.academy.scrapper.service;

import reactor.core.publisher.Mono;

@FunctionalInterface
public interface TrackerService {
    Mono<String> track(String url);
}
