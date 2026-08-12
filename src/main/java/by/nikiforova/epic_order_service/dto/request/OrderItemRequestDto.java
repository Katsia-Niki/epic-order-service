package by.nikiforova.epic_order_service.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderItemRequestDto (@NotNull Long itemId,
                                   @Positive Integer quantity) {
}
