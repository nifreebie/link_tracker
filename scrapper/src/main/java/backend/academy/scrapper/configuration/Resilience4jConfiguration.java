package backend.academy.scrapper.configuration;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Resilience4jConfiguration {
    @Bean
    public CircuitBreaker botClientCircuitBreak(CircuitBreakerRegistry registry) {
        return registry.circuitBreaker("botClientCircuit");
    }
}
