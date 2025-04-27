package backend.academy.scrapper.openapi.src.main.java.com.baeldung.openapi.api;

import jakarta.annotation.Generated;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.NativeWebRequest;

/**
 * A delegate to be called by the {@link TgChatApiController}}. Implement this interface with a
 * {@link org.springframework.stereotype.Service} annotated class.
 */
@Generated(
        value = "org.openapitools.codegen.languages.SpringCodegen",
        date = "2025-04-23T13:12:14.088362900+03:00[Europe/Moscow]",
        comments = "Generator version: 7.8.0")
public interface TgChatApiDelegate {

    default Optional<NativeWebRequest> getRequest() {
        return Optional.empty();
    }

    /**
     * DELETE /tg-chat/{id} : Удалить чат
     *
     * @param id (required)
     * @return Чат успешно удалён (status code 200) or Некорректные параметры запроса (status code 400) or Чат не
     *     существует (status code 404)
     * @see TgChatApi#tgChatIdDelete
     */
    default ResponseEntity<Void> tgChatIdDelete(Long id) {
        getRequest().ifPresent(request -> {
            for (MediaType mediaType : MediaType.parseMediaTypes(request.getHeader("Accept"))) {
                if (mediaType.isCompatibleWith(MediaType.valueOf("application/json"))) {
                    String exampleString =
                            "{ \"code\" : \"code\", \"stacktrace\" : [ \"stacktrace\", \"stacktrace\" ], \"description\" : \"description\", \"exceptionMessage\" : \"exceptionMessage\", \"exceptionName\" : \"exceptionName\" }";
                    ApiUtil.setExampleResponse(request, "application/json", exampleString);
                    break;
                }
                if (mediaType.isCompatibleWith(MediaType.valueOf("application/json"))) {
                    String exampleString =
                            "{ \"code\" : \"code\", \"stacktrace\" : [ \"stacktrace\", \"stacktrace\" ], \"description\" : \"description\", \"exceptionMessage\" : \"exceptionMessage\", \"exceptionName\" : \"exceptionName\" }";
                    ApiUtil.setExampleResponse(request, "application/json", exampleString);
                    break;
                }
            }
        });
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    /**
     * POST /tg-chat/{id} : Зарегистрировать чат
     *
     * @param id (required)
     * @return Чат зарегистрирован (status code 200) or Некорректные параметры запроса (status code 400)
     * @see TgChatApi#tgChatIdPost
     */
    default ResponseEntity<Void> tgChatIdPost(Long id) {
        getRequest().ifPresent(request -> {
            for (MediaType mediaType : MediaType.parseMediaTypes(request.getHeader("Accept"))) {
                if (mediaType.isCompatibleWith(MediaType.valueOf("application/json"))) {
                    String exampleString =
                            "{ \"code\" : \"code\", \"stacktrace\" : [ \"stacktrace\", \"stacktrace\" ], \"description\" : \"description\", \"exceptionMessage\" : \"exceptionMessage\", \"exceptionName\" : \"exceptionName\" }";
                    ApiUtil.setExampleResponse(request, "application/json", exampleString);
                    break;
                }
            }
        });
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }
}
