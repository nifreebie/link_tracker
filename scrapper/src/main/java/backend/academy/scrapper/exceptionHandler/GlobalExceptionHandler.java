package backend.academy.scrapper.exceptionHandler;

import backend.academy.scrapper.dto.response.ApiErrorResponse;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorResponse(
                        "Некорректные параметры запроса",
                        e.getErrorCode(),
                        e.getClass().getSimpleName(),
                        e.getMessage(),
                        Stream.of(e.getStackTrace())
                                .map(StackTraceElement::toString)
                                .collect(Collectors.toList())));
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingRequest(MissingRequestHeaderException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorResponse(
                        "Некорректные параметры запроса",
                        "400",
                        e.getClass().getSimpleName(),
                        e.getMessage(),
                        Stream.of(e.getStackTrace())
                                .map(StackTraceElement::toString)
                                .collect(Collectors.toList())));
    }
}
