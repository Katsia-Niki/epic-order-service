package by.nikiforova.epic_order_service.dto.response;

import by.nikiforova.epic_order_service.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponseDto (Long id,
                                Long userId,
                                OrderStatus status,
                                BigDecimal totalPrice,
                                List<OrderItemResponseDto> orderItems,
                                LocalDateTime createdAt,
                                LocalDateTime updatedAt) {
}
