package by.nikiforova.epic_order_service.dto.request;

import by.nikiforova.epic_order_service.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record OrderUpdateRequestDto (@NotNull OrderStatus status) {
}
