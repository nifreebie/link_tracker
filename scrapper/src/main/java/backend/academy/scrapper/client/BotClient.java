package backend.academy.scrapper.client;

import backend.academy.scrapper.model.dto.EventDTO;
import backend.academy.scrapper.model.dto.LinkDTO;

public interface BotClient {
    void update(LinkDTO link, EventDTO eventDTO);
}
