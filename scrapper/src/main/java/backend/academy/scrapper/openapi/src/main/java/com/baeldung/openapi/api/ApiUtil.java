package backend.academy.scrapper.openapi.src.main.java.com.baeldung.openapi.api;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.web.context.request.NativeWebRequest;

public class ApiUtil {
    public static void setExampleResponse(NativeWebRequest req, String contentType, String example) {
        try {
            HttpServletResponse res = req.getNativeResponse(HttpServletResponse.class);
            if (res != null) {
                res.setCharacterEncoding("UTF-8");
                res.addHeader("Content-Type", contentType);
                String safeExample = StringEscapeUtils.escapeHtml4(example);
                res.getWriter().print(safeExample);
            } else {
                System.err.println("HttpServletResponse is null in ApiUtil.setExampleResponse()");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
