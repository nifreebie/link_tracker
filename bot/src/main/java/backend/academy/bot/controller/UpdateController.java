package backend.academy.bot.controller;

import backend.academy.bot.model.dto.request.LinkUpdateRequest;
import backend.academy.bot.openapi.src.main.java.com.baeldung.openapi.api.UpdatesApi;
import backend.academy.bot.service.UpdateService;
import backend.academy.bot.util.BotMessages;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class UpdateController implements UpdatesApi, BotMessages {

    private final UpdateService updateService;

    @Override
    @PostMapping("/updates")
    @RateLimiter(name = "publicApi")
    public ResponseEntity<String> updatesPost(@RequestBody LinkUpdateRequest request) {
        updateService.sendUpdates(request);
        return ResponseEntity.ok(UPDATE_OK_RESPONSE);
    }
}
