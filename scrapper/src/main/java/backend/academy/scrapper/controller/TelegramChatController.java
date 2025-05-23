package backend.academy.scrapper.controller;

import backend.academy.scrapper.exceptions.IsAlreadyRegisteredException;
import backend.academy.scrapper.exceptions.NotFoundException;
import backend.academy.scrapper.model.dto.response.ApiErrorResponse;
import backend.academy.scrapper.openapi.src.main.java.com.baeldung.openapi.api.TgChatApi;
import backend.academy.scrapper.service.TelegramChatService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tg-chat")
@RequiredArgsConstructor
public class TelegramChatController implements TgChatApi {

    private final TelegramChatService telegramChatService;

    @Override
    @PostMapping("/{id}")
    @RateLimiter(name = "publicApi")
    public ResponseEntity<?> tgChatIdPost(@PathVariable Long id) {
        try {
            telegramChatService.register(id);
            return ResponseEntity.ok("Чат зарегистрирован");
        } catch (IsAlreadyRegisteredException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiErrorResponse(
                            "Конфликт",
                            "409",
                            e.getClass().getSimpleName(),
                            e.getMessage(),
                            Stream.of(e.getStackTrace())
                                    .map(StackTraceElement::toString)
                                    .collect(Collectors.toList())));
        }
    }

    @Override
    @DeleteMapping("/{id}")
    @RateLimiter(name = "publicApi")
    public ResponseEntity<?> tgChatIdDelete(@PathVariable Long id) {
        try {
            telegramChatService.delete(id);
            return ResponseEntity.ok("Чат успешно удалён");
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiErrorResponse(
                            "Ресурс не найден",
                            "404",
                            e.getClass().getSimpleName(),
                            e.getMessage(),
                            Stream.of(e.getStackTrace())
                                    .map(StackTraceElement::toString)
                                    .collect(Collectors.toList())));
        }
    }

    @GetMapping
    @RateLimiter(name = "publicApi")
    public ResponseEntity<List<Long>> getAllUsers() {
        return ResponseEntity.ok(telegramChatService.getAllUsers());
    }
}
