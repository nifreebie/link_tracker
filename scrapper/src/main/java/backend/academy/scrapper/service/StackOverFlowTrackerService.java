package backend.academy.scrapper.service;

import backend.academy.scrapper.model.dto.EventDTO;
import reactor.core.publisher.Mono;

public interface StackOverFlowTrackerService {
    Mono<EventDTO> trackAnswers(String url);

    Mono<EventDTO> trackComments(String url);
}
