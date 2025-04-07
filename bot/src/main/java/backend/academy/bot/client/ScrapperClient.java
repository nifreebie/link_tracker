package backend.academy.bot.client;

import backend.academy.bot.model.dto.response.ListLinksResponse;
import java.util.List;
import reactor.core.publisher.Mono;

public interface ScrapperClient {
    Mono<String> register(Long id);

    Mono<ListLinksResponse> getUserLinks(Long id);

    Mono<String> track(Long id, String url, List<String> tags, List<String> filters);

    Mono<String> untrack(Long id, String url);

    Mono<String> createTag(Long id, String tagName);

    Mono<List<String>> getUserTags(Long id);

    Mono<List<Long>> getAllUsers();

    Mono<String> addTags(Long id, String url, List<String> tags);

    Mono<String> removeTags(Long id, String url, List<String> tags);
}
