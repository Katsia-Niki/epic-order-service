package by.nikiforova.epic_order_service.dto.response;

import java.math.BigDecimal;

public record OrderItemResponseDto (Long itemId,
                                    String name,
                                    BigDecimal price,
                                    Integer quantity) {
}
