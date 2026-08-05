package by.nikiforova.epic_order_service.controller;

import by.nikiforova.epic_order_service.dto.request.OrderCreateRequestDto;
import by.nikiforova.epic_order_service.dto.request.OrderUpdateRequestDto;
import by.nikiforova.epic_order_service.dto.response.OrderWithUserResponseDto;
import by.nikiforova.epic_order_service.entity.OrderStatus;
import by.nikiforova.epic_order_service.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<Page<OrderWithUserResponseDto>> getAllOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) LocalDateTime createdFrom,
            @RequestParam(required = false) LocalDateTime createdTo,
            Pageable pageable) {
        return ResponseEntity.ok(orderService.getAll(status, createdFrom, createdTo, pageable));
    }

    @PostMapping
    public ResponseEntity<OrderWithUserResponseDto> createOrder(@Valid @RequestBody OrderCreateRequestDto dto) {
        OrderWithUserResponseDto createdOrder = orderService.createOrder(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderWithUserResponseDto> getOrderById(
            @PathVariable Long id) {
        return ResponseEntity.ok(orderService.getById(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<OrderWithUserResponseDto> updateOrder(@PathVariable Long id,
            @Valid @RequestBody OrderUpdateRequestDto dto) {
        return ResponseEntity.ok(orderService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        orderService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderWithUserResponseDto>> getOrdersByUserId(
            @PathVariable Long userId) {
        return ResponseEntity.ok(orderService.getByUserId(userId));
    }
}
