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
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collection;
import java.util.List;

import static by.nikiforova.epic_order_service.constant.Constants.HEADER_AUTHORIZATION;

@Slf4j
@Component
public class UserServiceClient {

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

    @CircuitBreaker(name = "userService", fallbackMethod = "getUsersByIdsFallback")
    public List<UserInfoDto> getUsersByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        var request = restClient.get()
                .uri(UriComponentsBuilder
                        .fromUriString(userServiceUrl + "/api/users/by-ids")
                        .queryParam("ids", ids)
                        .build()
                        .toUri());

        String auth = currentAuthorization();

        if (auth != null) {
            request = request.header(HEADER_AUTHORIZATION, auth);
        }
        UserInfoDto[] body = request.retrieve().body(UserInfoDto[].class);

        return body == null ? List.of() : List.of(body);
    }

    private UserInfoDto getUserByEmailFallback(String email, Throwable t) {
        log.warn("Circuit breaker getUserByEmailFallback triggered: {}", t.getMessage());
        throw new EntityNotFoundException(t.getMessage());
    }

    private UserInfoDto getUserByIdFallback(Long userId, Throwable t) {
        log.warn("Circuit breaker getUserByIdFallback triggered: {}", t.getMessage());
        throw new EntityNotFoundException(t.getMessage());
    }

    private List<UserInfoDto> getUsersByIdsFallback(Collection<Long> ids, Throwable t) {
        log.warn("Circuit breaker getUsersByIdsFallback triggered: {}", t.getMessage());
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
