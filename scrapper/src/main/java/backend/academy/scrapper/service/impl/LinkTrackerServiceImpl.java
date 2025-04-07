package backend.academy.scrapper.service.impl;

import backend.academy.scrapper.client.BotClient;
import backend.academy.scrapper.model.domain.EventType;
import backend.academy.scrapper.model.dto.EventDTO;
import backend.academy.scrapper.model.dto.LinkDTO;
import backend.academy.scrapper.repository.LinkRepository;
import backend.academy.scrapper.service.LinkTrackerService;
import backend.academy.scrapper.service.StackOverFlowTrackerService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@Slf4j
public class LinkTrackerServiceImpl implements LinkTrackerService {

    private final LinkRepository linkRepository;

    private final GitHubTrackerService gitHubTrackerService;

    private final StackOverFlowTrackerService stackOverFlowTrackerService;

    private final BotClient botClient;

    private static final Integer THREADS = 4;

    @Value("${app.batch-size}")
    private int batchSize;

    @Autowired
    public LinkTrackerServiceImpl(
            LinkRepository linkRepository,
            GitHubTrackerService gitHubTrackerService,
            StackOverFlowTrackerService stackOverFlowTrackerService,
            BotClient botClient) {
        this.linkRepository = linkRepository;
        this.gitHubTrackerService = gitHubTrackerService;
        this.stackOverFlowTrackerService = stackOverFlowTrackerService;
        this.botClient = botClient;
    }

    @Scheduled(fixedRate = 10000)
    public void checkForUpdates() {
        ExecutorService executor = Executors.newFixedThreadPool(THREADS);
        int offset = 0;
        List<LinkDTO> batch;

        do {
            batch = linkRepository.getPaginatedLinks(offset, batchSize);
            if (batch.isEmpty()) {
                break;
            }

            int subBatchSize = (int) Math.ceil((double) batch.size() / THREADS);
            List<Future<?>> futures = new ArrayList<>();
            for (int i = 0; i < THREADS; i++) {
                int fromIndex = i * subBatchSize;
                if (fromIndex >= batch.size()) {
                    break;
                }
                int toIndex = Math.min(fromIndex + subBatchSize, batch.size());
                List<LinkDTO> subBatch = batch.subList(fromIndex, toIndex);

                futures.add(executor.submit(() -> processBatch(subBatch)));
            }

            for (var future : futures) {
                try {
                    future.get();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } catch (ExecutionException e) {
                    log.error("Error while tracking link updates");
                }
            }

        } while (batch.size() == batchSize);

        executor.shutdown();
    }

    private void processBatch(List<LinkDTO> paginatedLinks) {
        paginatedLinks.forEach(link -> {
            switch (link.linkType()) {
                case GITHUB -> {
                    Mono<LocalDateTime> lastCommitDate = gitHubTrackerService
                            .trackCommit(link.url())
                            .filter(commit -> commit.date().isAfter(link.lastUpdatedAt()))
                            .publishOn(Schedulers.boundedElastic())
                            .doOnNext(commit -> botClient.update(link, commit))
                            .map(EventDTO::date)
                            .defaultIfEmpty(link.lastUpdatedAt());

                    Mono<LocalDateTime> lastEventDate = gitHubTrackerService
                            .trackEvent(link.url())
                            .filter(event -> event.date().isAfter(link.lastUpdatedAt())
                                    && event.eventType() != EventType.NO_CHANGES)
                            .publishOn(Schedulers.boundedElastic())
                            .doOnNext(event -> botClient.update(link, event))
                            .map(EventDTO::date)
                            .defaultIfEmpty(link.lastUpdatedAt());

                    Mono.zip(lastCommitDate, lastEventDate)
                            .map(dates -> dates.getT1().isAfter(dates.getT2()) ? dates.getT1() : dates.getT2())
                            .publishOn(Schedulers.boundedElastic())
                            .doOnNext(newLastUpdated -> linkRepository.updateLastUpdatedAt(link.id(), newLastUpdated))
                            .subscribe();
                }

                case STACKOVERFLOW -> {
                    Mono<LocalDateTime> lastAnswerDate = stackOverFlowTrackerService
                            .trackAnswers(link.url())
                            .filter(answer -> answer.date().isAfter(link.lastUpdatedAt()))
                            .publishOn(Schedulers.boundedElastic())
                            .doOnNext(commit -> botClient.update(link, commit))
                            .map(EventDTO::date)
                            .defaultIfEmpty(link.lastUpdatedAt());

                    Mono<LocalDateTime> lastCommentDate = stackOverFlowTrackerService
                            .trackComments(link.url())
                            .filter(comment -> comment.date().isAfter(link.lastUpdatedAt()))
                            .publishOn(Schedulers.boundedElastic())
                            .doOnNext(commit -> botClient.update(link, commit))
                            .map(EventDTO::date)
                            .defaultIfEmpty(link.lastUpdatedAt());

                    Mono.zip(lastAnswerDate, lastCommentDate)
                            .map(dates -> dates.getT1().isAfter(dates.getT2()) ? dates.getT1() : dates.getT2())
                            .publishOn(Schedulers.boundedElastic())
                            .doOnNext(newLastUpdated -> linkRepository.updateLastUpdatedAt(link.id(), newLastUpdated))
                            .subscribe();
                }
            }
        });
    }
}
