package by.nikiforova.epic_order_service.service;

import by.nikiforova.epic_order_service.client.UserServiceClient;
import by.nikiforova.epic_order_service.dto.response.OrderResponseDto;
import by.nikiforova.epic_order_service.dto.response.OrderWithUserResponseDto;
import by.nikiforova.epic_order_service.dto.response.UserInfoDto;
import by.nikiforova.epic_order_service.entity.Order;
import by.nikiforova.epic_order_service.entity.OrderStatus;
import by.nikiforova.epic_order_service.exception.EntityNotFoundException;
import by.nikiforova.epic_order_service.mapper.OrderMapper;
import by.nikiforova.epic_order_service.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderCacheServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private UserServiceClient userServiceClient;
    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderCacheService orderCacheService;

    private Order order;
    private UserInfoDto userInfo;
    private OrderResponseDto orderResponseDto;

    @BeforeEach
    void setUp() {
        order = Order.builder()
                .userId(1L)
                .status(OrderStatus.CREATED)
                .totalPrice(new BigDecimal("30.00"))
                .deleted(false)
                .build();
        order.setId(10L);

        userInfo = new UserInfoDto(
                1L, "Ivan", "Ivanov", "ivan.mail@gmail.com",
                LocalDate.of(1992, Month.JUNE, 12), true,
                LocalDateTime.now(), LocalDateTime.now()
        );

        orderResponseDto = new OrderResponseDto(10L, 1L, OrderStatus.CREATED,
                new BigDecimal("30.00"), List.of(), null, null);
    }

    @Test
    @DisplayName("get by id - success")
    void getByIdSuccess() {
        when(orderRepository.findByIdAndDeletedFalse(10L)).thenReturn(Optional.of(order));
        when(userServiceClient.getUserById(1L)).thenReturn(userInfo);
        when(orderMapper.toResponseDto(order)).thenReturn(orderResponseDto);

        OrderWithUserResponseDto result = orderCacheService.getById(10L);

        assertEquals(orderResponseDto, result.order());
        assertEquals(userInfo, result.user());

        verify(orderRepository).findByIdAndDeletedFalse(10L);
        verify(userServiceClient).getUserById(1L);
        verify(orderMapper).toResponseDto(order);
    }

    @Test
    @DisplayName("get by id - EntityNotFoundException")
    void getByIdShouldThrowEntityNotFoundException() {
        when(orderRepository.findByIdAndDeletedFalse(10L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> orderCacheService.getById(10L));

        assertEquals("Order not found 10", exception.getMessage());

        verify(orderRepository).findByIdAndDeletedFalse(10L);
        verify(userServiceClient, never()).getUserById(any());
        verify(orderMapper, never()).toResponseDto(any());
    }

    @Test
    @DisplayName("evict - does not throw")
    void evictDoesNotThrow() {

        assertDoesNotThrow(() -> orderCacheService.evict(10L));
    }
}
