package backend.academy.bot.client;

import backend.academy.bot.dto.response.ListLinksResponse;
import java.util.List;
import reactor.core.publisher.Mono;

public interface ScrapperClient {
    Mono<String> register(Long id);

    Mono<ListLinksResponse> getUserLinks(Long id);

    Mono<String> track(Long id, String url, List<String> tags, List<String> filters);

    Mono<String> untrack(Long id, String url);
}
