package backend.academy.scrapper.controller;

import backend.academy.scrapper.exceptions.IsAlreadyRegisteredException;
import backend.academy.scrapper.exceptions.NotFoundException;
import backend.academy.scrapper.model.dto.LinkDTO;
import backend.academy.scrapper.model.dto.request.AddLinkRequest;
import backend.academy.scrapper.model.dto.request.ChangeLinkTagsRequest;
import backend.academy.scrapper.model.dto.request.RemoveLinkRequest;
import backend.academy.scrapper.model.dto.response.ApiErrorResponse;
import backend.academy.scrapper.model.dto.response.LinkResponse;
import backend.academy.scrapper.model.dto.response.ListLinksResponse;
import backend.academy.scrapper.service.LinkService;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/links")
public class LinkController {
    private final LinkService linkService;

    @Autowired
    public LinkController(LinkService linkService) {
        this.linkService = linkService;
    }

    @GetMapping
    public ResponseEntity<?> getUserLinks(@RequestHeader("Tg-Chat-Id") Long telegramChatId) {
        List<LinkResponse> response = new ArrayList<>();
        linkService.getUserLinks(telegramChatId).forEach(link -> response.add(link.toResponse()));
        return ResponseEntity.ok(new ListLinksResponse(response, response.size()));
    }

    @PostMapping
    public ResponseEntity<?> track(
            @RequestHeader("Tg-Chat-Id") Long telegramChatId, @RequestBody AddLinkRequest request) {
        try {
            LinkDTO link = linkService.follow(request, telegramChatId);
            return ResponseEntity.ok(link.toResponse());
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

    @DeleteMapping
    public ResponseEntity<?> untrack(
            @RequestHeader("Tg-Chat-Id") Long telegramChatId, @RequestBody RemoveLinkRequest request) {
        try {
            LinkDTO removedLink = linkService.unfollow(request.link(), telegramChatId);
            return ResponseEntity.ok(removedLink.toResponse());
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

    @PostMapping("/tags")
    public ResponseEntity<?> addTags(
            @RequestHeader("Tg-Chat-Id") Long telegramChatId, @RequestBody ChangeLinkTagsRequest request) {
        try {
            request.tags().forEach(tag -> linkService.addLinkTag(tag, request.url(), telegramChatId));
            return ResponseEntity.ok("Тэги успешно добавлены");
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

    @DeleteMapping("/tags")
    public ResponseEntity<?> removeTags(
            @RequestHeader("Tg-Chat-Id") Long telegramChatId, @RequestBody ChangeLinkTagsRequest request) {
        try {
            request.tags().forEach(tag -> linkService.removeLinkTag(tag, request.url(), telegramChatId));
            return ResponseEntity.ok("Тэги успешно удалены");
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
}
