package backend.academy.scrapper.service.impl;

import backend.academy.scrapper.client.StackOverFlowClient;
import backend.academy.scrapper.model.dto.EventDTO;
import backend.academy.scrapper.service.StackOverFlowTrackerService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class StackOverFlowTrackerServiceImpl implements StackOverFlowTrackerService {
    private final StackOverFlowClient stackOverFlowClient;

    @Autowired
    public StackOverFlowTrackerServiceImpl(StackOverFlowClient stackOverFlowClient) {
        this.stackOverFlowClient = stackOverFlowClient;
    }

    @Override
    public Mono<EventDTO> trackAnswers(String url) {
        String id = extractQuestionId(url);
        return stackOverFlowClient.getQuestionLastAnswer(id);
    }

    @Override
    public Mono<EventDTO> trackComments(String url) {
        String id = extractQuestionId(url);
        return stackOverFlowClient.getQuestionLastComment(id);
    }

    private String extractQuestionId(String url) {
        Pattern pattern = Pattern.compile("/questions/(\\d+)/");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new IllegalArgumentException("Invalid SO URL format");
    }
}
