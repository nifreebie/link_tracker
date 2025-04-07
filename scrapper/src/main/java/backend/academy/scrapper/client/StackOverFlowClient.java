package backend.academy.scrapper.client;

import backend.academy.scrapper.model.dto.EventDTO;
import reactor.core.publisher.Mono;

public interface StackOverFlowClient {
    Mono<EventDTO> getQuestionLastAnswer(String id);

    Mono<EventDTO> getQuestionLastComment(String id);
}
