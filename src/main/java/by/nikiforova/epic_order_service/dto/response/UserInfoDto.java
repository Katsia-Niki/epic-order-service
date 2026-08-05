package by.nikiforova.epic_order_service.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDate;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UserInfoDto (Long id,
                          String name,
                          String surname,
                          String email,
                          LocalDate birthDate,
                          Boolean active,
                          LocalDateTime createdAt,
                          LocalDateTime updatedAt) {
}
