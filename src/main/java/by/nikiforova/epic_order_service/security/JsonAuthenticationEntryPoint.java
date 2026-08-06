package by.nikiforova.epic_order_service.security;

import by.nikiforova.epic_order_service.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static by.nikiforova.epic_order_service.constant.Constants.TIMEZONE;

@Component
public class JsonAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                         @NonNull AuthenticationException authException) throws IOException {

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ErrorResponse body = new ErrorResponse(LocalDateTime.now(ZoneId.of(TIMEZONE)),
                                    HttpStatus.UNAUTHORIZED.value(),
                                    "Unauthorized",
                                    request.getRequestURI(),
                                    null);

        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
