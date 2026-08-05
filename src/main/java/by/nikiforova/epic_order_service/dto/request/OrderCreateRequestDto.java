package by.nikiforova.epic_order_service.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OrderCreateRequestDto (@NotNull @Email String email,
                                     @NotEmpty @Valid List<OrderItemRequestDto> orderItems) {
}
