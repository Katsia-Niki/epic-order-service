package by.nikiforova.epic_order_service.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ItemRequestDto(
        @NotBlank String name,
        @NotNull @DecimalMin("0.01") BigDecimal price
) {
}
