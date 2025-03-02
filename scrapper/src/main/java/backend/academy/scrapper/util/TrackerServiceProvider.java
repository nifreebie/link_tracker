package backend.academy.scrapper.util;

import backend.academy.scrapper.model.LinkType;
import backend.academy.scrapper.service.TrackerService;
import backend.academy.scrapper.service.impl.GitHubTrackerService;
import backend.academy.scrapper.service.impl.StackOverFlowTrackerService;
import jakarta.annotation.PostConstruct;
import java.util.EnumMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class TrackerServiceProvider {
    private Map<LinkType, TrackerService> trackers;

    private final GitHubTrackerService gitHubTrackerService;

    private final StackOverFlowTrackerService stackOverFlowTrackerService;

    @Autowired
    public TrackerServiceProvider(
            GitHubTrackerService gitHubTrackerService, StackOverFlowTrackerService stackOverFlowTrackerService) {
        this.gitHubTrackerService = gitHubTrackerService;
        this.stackOverFlowTrackerService = stackOverFlowTrackerService;
    }

    @PostConstruct
    private void init() {
        trackers = new EnumMap<>(LinkType.class);
        trackers.put(LinkType.GITHUB, gitHubTrackerService);
        trackers.put(LinkType.STACKOVERFLOW, stackOverFlowTrackerService);
    }

    public Mono<String> provide(String url) {
        return trackers.get(LinkType.getLinkType(url)).track(url);
    }
}
