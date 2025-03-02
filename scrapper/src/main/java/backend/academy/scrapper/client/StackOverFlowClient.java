package backend.academy.scrapper.client;

import reactor.core.publisher.Mono;

public interface StackOverFlowClient {
    Mono<String> getQuestionLastUpdated(String id);
}
