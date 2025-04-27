package backend.academy.bot.openapi.src.main.java.com.baeldung.openapi.api;

import jakarta.annotation.Generated;
import jakarta.validation.constraints.*;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Generated(
        value = "org.openapitools.codegen.languages.SpringCodegen",
        date = "2025-04-23T11:51:06.950796200Z[UTC]",
        comments = "Generator version: 7.8.0")
@Controller
@RequestMapping("${openapi.bot.base-path:}")
public class UpdatesApiController implements UpdatesApi {

    private final UpdatesApiDelegate delegate;

    public UpdatesApiController(@Autowired(required = false) UpdatesApiDelegate delegate) {
        this.delegate = Optional.ofNullable(delegate).orElse(new UpdatesApiDelegate() {});
    }

    @Override
    public UpdatesApiDelegate getDelegate() {
        return delegate;
    }
}
