package backend.academy.scrapper.service.impl;

import backend.academy.scrapper.client.BotClient;
import backend.academy.scrapper.repository.LinkRepository;
import backend.academy.scrapper.service.LinkTrackerService;
import backend.academy.scrapper.util.TrackerServiceProvider;
import jakarta.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class LinkTrackerServiceImpl implements LinkTrackerService {
    private final TrackerServiceProvider trackerServiceProvider;

    private final LinkRepository linkRepository;

    private Map<String, String> lastUpdatedMap;

    private final BotClient botClient;

    @Autowired
    public LinkTrackerServiceImpl(
            TrackerServiceProvider trackerServiceProvider, LinkRepository linkRepository, BotClient botClient) {
        this.trackerServiceProvider = trackerServiceProvider;
        this.linkRepository = linkRepository;
        this.botClient = botClient;
    }

    @PostConstruct
    public void init() {
        lastUpdatedMap = new ConcurrentHashMap<>();
    }

    @Scheduled(fixedRate = 1000)
    public void checkForUpdates() {
        linkRepository
                .getAll()
                .forEach(link -> trackerServiceProvider.provide(link.url()).subscribe(updatedAt -> {
                    if (lastUpdatedMap.containsKey(link.url())) {
                        String lastUpdated = lastUpdatedMap.get(link.url());
                        if (!lastUpdated.equals(updatedAt)) {
                            lastUpdatedMap.put(link.url(), updatedAt);
                            botClient.update(link);
                        }
                    } else {
                        lastUpdatedMap.put(link.url(), updatedAt);
                    }
                }));
    }
}
