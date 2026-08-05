package by.nikiforova.epic_order_service.client;

import by.nikiforova.epic_order_service.dto.response.UserInfoDto;
import by.nikiforova.epic_order_service.exception.EntityNotFoundException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Component
public class UserServiceClient {

    private static final String HEADER_AUTHORIZATION = "Authorization";
    private final RestClient restClient = RestClient.builder().build();

    @Value("${user.service.url}")
    private String userServiceUrl;

    @CircuitBreaker(name = "userService", fallbackMethod = "getUserByEmailFallback")
    public UserInfoDto getUserByEmail(String email) {

        var request = restClient.get()
                .uri(userServiceUrl + "/api/users/email/{email}", email);

        String auth = currentAuthorization();
        if (auth != null) {
            request = request.header(HEADER_AUTHORIZATION, auth);
        }

        return request.retrieve().body(UserInfoDto.class);
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "getUserByIdFallback")
    public UserInfoDto getUserById(Long userId) {

        var request = restClient.get()
                .uri(userServiceUrl + "/api/users/{id}", userId);

        String auth = currentAuthorization();
        if (auth != null) {
            request = request.header(HEADER_AUTHORIZATION, auth);
        }

        return request.retrieve().body(UserInfoDto.class);
    }

    private UserInfoDto getUserByEmailFallback(String email, Throwable t) {
        log.warn("Circuit breaker getUserByEmailFallback triggered: {}", t.getMessage());
        throw new EntityNotFoundException(t.getMessage());
    }

    private UserInfoDto getUserByIdFallback(Long userId, Throwable t) {
        log.warn("Circuit breaker getUserByIdFallback triggered: {}", t.getMessage());
        throw new EntityNotFoundException(t.getMessage());
    }

    private String currentAuthorization() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return null;
        }
        return attrs.getRequest().getHeader(HEADER_AUTHORIZATION);
    }
}
