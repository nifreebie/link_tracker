package backend.academy.scrapper.openapi.src.main.java.com.baeldung.openapi.api;

import backend.academy.scrapper.model.dto.request.AddLinkRequest;
import backend.academy.scrapper.model.dto.request.RemoveLinkRequest;
import backend.academy.scrapper.model.dto.response.LinkResponse;
import backend.academy.scrapper.model.dto.response.ListLinksResponse;
import jakarta.annotation.Generated;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.NativeWebRequest;

/**
 * A delegate to be called by the {@link LinksApiController}}. Implement this interface with a
 * {@link org.springframework.stereotype.Service} annotated class.
 */
@Generated(
        value = "org.openapitools.codegen.languages.SpringCodegen",
        date = "2025-04-23T13:12:14.088362900+03:00[Europe/Moscow]",
        comments = "Generator version: 7.8.0")
public interface LinksApiDelegate {

    default Optional<NativeWebRequest> getRequest() {
        return Optional.empty();
    }

    /**
     * DELETE /links : Убрать отслеживание ссылки
     *
     * @param tgChatId (required)
     * @param removeLinkRequest (required)
     * @return Ссылка успешно убрана (status code 200) or Некорректные параметры запроса (status code 400) or Ссылка не
     *     найдена (status code 404)
     * @see LinksApi#linksDelete
     */
    default ResponseEntity<LinkResponse> linksDelete(Long tgChatId, RemoveLinkRequest removeLinkRequest) {
        getRequest().ifPresent(request -> {
            for (MediaType mediaType : MediaType.parseMediaTypes(request.getHeader("Accept"))) {
                if (mediaType.isCompatibleWith(MediaType.valueOf("application/json"))) {
                    String exampleString =
                            "{ \"id\" : 0, \"filters\" : [ \"filters\", \"filters\" ], \"url\" : \"https://openapi-generator.tech\", \"tags\" : [ \"tags\", \"tags\" ] }";
                    ApiUtil.setExampleResponse(request, "application/json", exampleString);
                    break;
                }
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
     * GET /links : Получить все отслеживаемые ссылки
     *
     * @param tgChatId (required)
     * @return Ссылки успешно получены (status code 200) or Некорректные параметры запроса (status code 400)
     * @see LinksApi#linksGet
     */
    default ResponseEntity<ListLinksResponse> linksGet(Long tgChatId) {
        getRequest().ifPresent(request -> {
            for (MediaType mediaType : MediaType.parseMediaTypes(request.getHeader("Accept"))) {
                if (mediaType.isCompatibleWith(MediaType.valueOf("application/json"))) {
                    String exampleString =
                            "{ \"size\" : 6, \"links\" : [ { \"id\" : 0, \"filters\" : [ \"filters\", \"filters\" ], \"url\" : \"https://openapi-generator.tech\", \"tags\" : [ \"tags\", \"tags\" ] }, { \"id\" : 0, \"filters\" : [ \"filters\", \"filters\" ], \"url\" : \"https://openapi-generator.tech\", \"tags\" : [ \"tags\", \"tags\" ] } ] }";
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
     * POST /links : Добавить отслеживание ссылки
     *
     * @param tgChatId (required)
     * @param addLinkRequest (required)
     * @return Ссылка успешно добавлена (status code 200) or Некорректные параметры запроса (status code 400)
     * @see LinksApi#linksPost
     */
    default ResponseEntity<LinkResponse> linksPost(Long tgChatId, AddLinkRequest addLinkRequest) {
        getRequest().ifPresent(request -> {
            for (MediaType mediaType : MediaType.parseMediaTypes(request.getHeader("Accept"))) {
                if (mediaType.isCompatibleWith(MediaType.valueOf("application/json"))) {
                    String exampleString =
                            "{ \"id\" : 0, \"filters\" : [ \"filters\", \"filters\" ], \"url\" : \"https://openapi-generator.tech\", \"tags\" : [ \"tags\", \"tags\" ] }";
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
}
