package backend.academy.bot.service;

import backend.academy.bot.model.dto.request.LinkUpdateRequest;

public interface UpdateService {
    void sendUpdates(LinkUpdateRequest request);
}
