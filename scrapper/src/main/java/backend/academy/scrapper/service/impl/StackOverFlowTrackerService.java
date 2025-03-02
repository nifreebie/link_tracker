package backend.academy.scrapper.service.impl;

import backend.academy.scrapper.client.StackOverFlowClient;
import backend.academy.scrapper.service.TrackerService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class StackOverFlowTrackerService implements TrackerService {
    private final StackOverFlowClient stackOverFlowClient;

    @Autowired
    public StackOverFlowTrackerService(StackOverFlowClient stackOverFlowClient) {
        this.stackOverFlowClient = stackOverFlowClient;
    }

    @Override
    public Mono<String> track(String url) {
        String id = extractQuestionId(url);
        return stackOverFlowClient.getQuestionLastUpdated(id);
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
