package backend.academy.scrapper.configuration;

import backend.academy.scrapper.ScrapperConfig;
import backend.academy.scrapper.interceptor.AuthInterceptor;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.PrematureCloseException;
import reactor.util.retry.Retry;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class WebConfiguration implements WebMvcConfigurer {
    private final AuthInterceptor authInterceptor;
    private final CircuitBreaker botCircuit;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor).addPathPatterns("/api/v1/links/**");
    }

    @Bean
    @Primary
    public WebClient.Builder webClientBuilder(
            @Value("${app.response-timeout}") Duration responseTimeout,
            @Value("${app.connection-timeout}") Duration connectionTimeout,
            @Value("${app.max-retries}") Integer maxRetries,
            @Value("${app.backoff}") Duration backoff,
            ScrapperConfig config) {
        ExchangeFilterFunction retryFilter = (request, next) -> next.exchange(request)
                .flatMap(resp -> {
                    HttpStatusCode status = resp.statusCode();
                    if (config.retryStatusCodes().contains(status.value())) {
                        return resp.createException().flatMap(Mono::error);
                    }
                    return Mono.just(resp);
                })
                .retryWhen(Retry.fixedDelay(maxRetries, backoff)
                        .filter(this::isRetryable)
                        .onRetryExhaustedThrow((spec, signal) -> signal.failure()))
                .transformDeferred(CircuitBreakerOperator.of(botCircuit));
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create()
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) connectionTimeout.toMillis())
                        .doOnConnected(conn -> conn.addHandlerLast(
                                        new ReadTimeoutHandler(responseTimeout.toMillis(), TimeUnit.MILLISECONDS))
                                .addHandlerLast(
                                        new WriteTimeoutHandler(responseTimeout.toMillis(), TimeUnit.MILLISECONDS)))
                        .responseTimeout(responseTimeout)))
                .filter(retryFilter);
    }

    private boolean isRetryable(Throwable t) {
        if (t instanceof WebClientResponseException wce) {
            HttpStatusCode s = wce.getStatusCode();
            if (s.is5xxServerError() || s == HttpStatus.TOO_MANY_REQUESTS) {
                return true;
            }
        }
        if (hasCause(t, IOException.class) || hasCause(t, TimeoutException.class)) {
            return true;
        }
        return hasCause(t, PrematureCloseException.class);
    }

    private boolean hasCause(Throwable t, Class<? extends Throwable> cls) {
        Throwable cause = t;
        while (cause != null) {
            if (cls.isInstance(cause)) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }
}
