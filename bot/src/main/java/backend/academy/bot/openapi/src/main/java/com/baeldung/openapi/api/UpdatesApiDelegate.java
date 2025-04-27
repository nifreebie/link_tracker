package backend.academy.bot.openapi.src.main.java.com.baeldung.openapi.api;

import backend.academy.bot.model.dto.request.LinkUpdateRequest;
import jakarta.annotation.Generated;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.NativeWebRequest;

/**
 * A delegate to be called by the {@link UpdatesApiController}}. Implement this interface with a
 * {@link org.springframework.stereotype.Service} annotated class.
 */
@Generated(
        value = "org.openapitools.codegen.languages.SpringCodegen",
        date = "2025-04-23T11:51:06.950796200Z[UTC]",
        comments = "Generator version: 7.8.0")
public interface UpdatesApiDelegate {

    default Optional<NativeWebRequest> getRequest() {
        return Optional.empty();
    }

    /**
     * POST /updates : Отправить обновление
     *
     * @param linkUpdate (required)
     * @return Обновление обработано (status code 200) or Некорректные параметры запроса (status code 400)
     * @see UpdatesApi#updatesPost
     */
    default ResponseEntity<String> updatesPost(LinkUpdateRequest linkUpdate) {
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
