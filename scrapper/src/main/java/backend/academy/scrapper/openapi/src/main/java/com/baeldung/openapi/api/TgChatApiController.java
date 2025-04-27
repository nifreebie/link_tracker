package backend.academy.scrapper.openapi.src.main.java.com.baeldung.openapi.api;

import jakarta.annotation.Generated;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Generated(
        value = "org.openapitools.codegen.languages.SpringCodegen",
        date = "2025-04-22T15:07:25.937293500Z[UTC]",
        comments = "Generator version: 7.8.0")
@Controller
@RequestMapping("${openapi.scrapper.base-path:}")
public class TgChatApiController implements TgChatApi {

    private final TgChatApiDelegate delegate;

    public TgChatApiController(@Autowired(required = false) TgChatApiDelegate delegate) {
        this.delegate = Optional.ofNullable(delegate).orElse(new TgChatApiDelegate() {});
    }

    @Override
    public TgChatApiDelegate getDelegate() {
        return delegate;
    }
}
