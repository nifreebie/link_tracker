package backend.academy.scrapper.controller;

import backend.academy.scrapper.exceptions.IsAlreadyRegisteredException;
import backend.academy.scrapper.model.dto.response.ApiErrorResponse;
import backend.academy.scrapper.service.impl.TagServiceImpl;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tags")
public class TagController {

    private final TagServiceImpl tagService;

    @Autowired
    public TagController(TagServiceImpl tagService) {
        this.tagService = tagService;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestHeader("Tg-Chat-Id") Long telegramChatId, @RequestBody String name) {
        try {
            return ResponseEntity.ok(tagService.create(name, telegramChatId));
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

    @GetMapping
    public ResponseEntity<?> getUserTags(@RequestHeader("Tg-Chat-Id") Long telegramChatId) {
        return ResponseEntity.ok(tagService.getUserTags(telegramChatId));
    }
}
