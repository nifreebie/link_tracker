package backend.academy.scrapper.model;

import backend.academy.scrapper.dto.response.LinkResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Link {
    private int id;
    private String url;
    private List<String> tags;
    private List<String> filters;
    private List<Integer> telegramChatIds = new ArrayList<>();

    public Link(String url, List<String> tags, List<String> filters, int telegramChatId) {
        this.id = ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE);
        this.url = url;
        this.tags = tags;
        this.filters = filters;
        this.telegramChatIds.add(telegramChatId);
    }

    public LinkResponse toDTO() {
        return new LinkResponse(id, url, tags, filters);
    }
}
