package backend.academy.scrapper.interceptor;

import backend.academy.scrapper.dto.response.ApiErrorResponse;
import backend.academy.scrapper.service.TelegramChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Collections;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final TelegramChatService telegramChatService;

    @Autowired
    public AuthInterceptor(TelegramChatService telegramChatService) {
        this.telegramChatService = telegramChatService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler)
            throws Exception {
        String chatIdHeader = request.getHeader("Tg-Chat-Id");

        if (chatIdHeader == null
                || !isNumeric(chatIdHeader)
                || !telegramChatService.isRegistered(Integer.parseInt(chatIdHeader))) {
            ApiErrorResponse errorResponse = new ApiErrorResponse(
                    "Доступ запрещен",
                    "403",
                    "UserNotRegisteredException",
                    "Пользователь не зарегистрирован",
                    Collections.emptyList());

            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(new ObjectMapper().writeValueAsString(errorResponse));
            return false;
        }
        return true;
    }

    private boolean isNumeric(String str) {
        return str.matches("\\d+");
    }
}
