package by.nikiforova.epic_order_service.service;

import by.nikiforova.epic_order_service.client.UserServiceClient;
import by.nikiforova.epic_order_service.dto.request.OrderCreateRequestDto;
import by.nikiforova.epic_order_service.dto.request.OrderItemRequestDto;
import by.nikiforova.epic_order_service.dto.request.OrderUpdateRequestDto;
import by.nikiforova.epic_order_service.dto.response.OrderResponseDto;
import by.nikiforova.epic_order_service.dto.response.OrderWithUserResponseDto;
import by.nikiforova.epic_order_service.dto.response.UserInfoDto;
import by.nikiforova.epic_order_service.entity.Item;
import by.nikiforova.epic_order_service.entity.Order;
import by.nikiforova.epic_order_service.entity.OrderStatus;
import by.nikiforova.epic_order_service.exception.EntityNotFoundException;
import by.nikiforova.epic_order_service.exception.ServiceUnavailableException;
import by.nikiforova.epic_order_service.mapper.OrderMapper;
import by.nikiforova.epic_order_service.repository.ItemRepository;
import by.nikiforova.epic_order_service.repository.OrderRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static by.nikiforova.epic_order_service.constant.Constants.PLACEHOLDER_EMAIL;
import static by.nikiforova.epic_order_service.constant.Constants.PLACEHOLDER_NAME;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    private static final String TIMEZONE = "Europe/Minsk";

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserServiceClient userServiceClient;
    @Mock
    private OrderMapper orderMapper;
    @Mock
    private OrderCacheService orderCacheService;

    @InjectMocks
    private OrderService orderService;

    private UserInfoDto userInfo;
    private Item item;
    private OrderCreateRequestDto createRequestDto;

    @BeforeEach
    void setUp() {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken("Ivan", null,
                List.of(new SimpleGrantedAuthority("ROLE_USER")));

        auth.setDetails(1L);
        SecurityContextHolder.getContext().setAuthentication(auth);

        userInfo = new UserInfoDto(
                1L, "Ivan", "Ivanov", "ivan.mail@gmail.com",
                LocalDate.of(1992, Month.JUNE, 12), true,
                LocalDateTime.now(), LocalDateTime.now()
        );

        item = Item.builder()
                .name("Socks")
                .price(new BigDecimal("10.00"))
                .build();

        item.setId(5L);

        createRequestDto = new OrderCreateRequestDto("ivan.mail@gmail.com",
                List.of(new OrderItemRequestDto(5L, 3)));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("create order - success")
    void createOrderShouldSaveOrderAndReturnOrderWithUser() {
        when(userServiceClient.getUserByEmail("ivan.mail@gmail.com")).thenReturn(userInfo);
        when(itemRepository.findById(5L)).thenReturn(Optional.of(item));

        Order savedOrder = Order.builder()
                .userId(1L)
                .status(OrderStatus.CREATED)
                .totalPrice(new BigDecimal("20.00"))
                .deleted(false)
                .build();
        savedOrder.setId(111L);

        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        OrderResponseDto orderResponseDto = new OrderResponseDto(111L, 1L, OrderStatus.CREATED,
                new BigDecimal("20.00"), List.of(), null, null);

        when(orderMapper.toResponseDto(any(Order.class))).thenReturn(orderResponseDto);

        OrderWithUserResponseDto result = orderService.createOrder(createRequestDto);

        assertEquals(orderResponseDto, result.order());
        assertEquals(userInfo, result.user());

        verify(orderRepository).save(any(Order.class));
        verify(userServiceClient).getUserByEmail("ivan.mail@gmail.com");
        verify(itemRepository).findById(5L);
    }

    @Test
    @DisplayName("create order - EntityNotFoundException")
    void createOrderShouldThrowEntityNotFoundException() {
        when(userServiceClient.getUserByEmail("ivan.mail@gmail.com")).thenReturn(userInfo);
        when(itemRepository.findById(5L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                                            () -> orderService.createOrder(createRequestDto));

        assertEquals("Item not found 5", exception.getMessage());

        verify(userServiceClient).getUserByEmail("ivan.mail@gmail.com");
        verify(itemRepository).findById(5L);
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("create order - AccessDeniedException")
    void createOrderShouldThrowAccessDeniedException() {

        UserInfoDto otherUser = new UserInfoDto(2L, "Ivan", "Ivanov", "ivan.mail@gmail.com",
                LocalDate.of(1992, Month.JUNE, 12), true,
                LocalDateTime.now(ZoneId.of(TIMEZONE)), LocalDateTime.now(ZoneId.of(TIMEZONE)));

        when(userServiceClient.getUserByEmail("ivan.mail@gmail.com")).thenReturn(otherUser);

        assertThrows(AccessDeniedException.class, () -> orderService.createOrder(createRequestDto));

        verify(userServiceClient).getUserByEmail("ivan.mail@gmail.com");
        verify(itemRepository, never()).findById(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("create order - ServiceUnavailableException when user placeholder")
    void createOrderShouldThrowServiceUnavailableWhenPlaceholderUser() {
        UserInfoDto placeholder = new UserInfoDto(
                null, PLACEHOLDER_NAME, PLACEHOLDER_NAME, PLACEHOLDER_EMAIL,
                null, null, null, null
        );
        when(userServiceClient.getUserByEmail("ivan.mail@gmail.com")).thenReturn(placeholder);

        assertThrows(ServiceUnavailableException.class,
                () -> orderService.createOrder(createRequestDto));

        verify(userServiceClient).getUserByEmail("ivan.mail@gmail.com");
        verify(itemRepository, never()).findById(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("get by id - success")
    void getOrderByIdShouldReturnOrderWithUser() {
        OrderResponseDto orderResponseDto = new OrderResponseDto(10L, 1L, OrderStatus.CREATED,
                new BigDecimal("30.00"), List.of(), null, null);
        OrderWithUserResponseDto cached = new OrderWithUserResponseDto(orderResponseDto, userInfo);

        when(orderCacheService.getById(10L)).thenReturn(cached);

        OrderWithUserResponseDto result = orderService.getById(10L);

        assertEquals(orderResponseDto, result.order());
        assertEquals(userInfo, result.user());
        verify(orderCacheService).getById(10L);
    }

    @Test
    @DisplayName("get by id - EntityNotFoundException")
    void getOrderByIdShouldThrowEntityNotFoundException() {
        when(orderCacheService.getById(10L))
                .thenThrow(new EntityNotFoundException("Order not found 10"));

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> orderService.getById(10L));

        assertEquals("Order not found 10", exception.getMessage());
        verify(orderCacheService).getById(10L);
    }

    @Test
    @DisplayName("get by id - returns order with placeholder user")
    void getOrderByIdShouldReturnPlaceholderUserWhenUserServiceUnavailable() {
        UserInfoDto placeholder = new UserInfoDto(
                1L, PLACEHOLDER_NAME, PLACEHOLDER_NAME, PLACEHOLDER_EMAIL,
                null, null, null, null
        );
        OrderResponseDto orderResponseDto = new OrderResponseDto(10L, 1L, OrderStatus.CREATED,
                new BigDecimal("30.00"), List.of(), null, null);
        when(orderCacheService.getById(10L))
                .thenReturn(new OrderWithUserResponseDto(orderResponseDto, placeholder));

        OrderWithUserResponseDto result = orderService.getById(10L);

        assertEquals(orderResponseDto, result.order());
        assertEquals(PLACEHOLDER_NAME, result.user().name());
        assertEquals(PLACEHOLDER_EMAIL, result.user().email());
        verify(orderCacheService).getById(10L);
    }

    @Test
    @DisplayName("get by id - AccessDeniedException")
    void getOrderByIdShouldThrowAccessDeniedException() {
        OrderResponseDto orderResponseDto = new OrderResponseDto(10L, 2L, OrderStatus.CREATED,
                new BigDecimal("30.00"), List.of(), null, null);
        when(orderCacheService.getById(10L))
                .thenReturn(new OrderWithUserResponseDto(orderResponseDto, userInfo));

        assertThrows(AccessDeniedException.class, () -> orderService.getById(10L));

        verify(orderCacheService).getById(10L);
    }

    @Test
    @DisplayName("get all - success")
    void getAllOrdersShouldReturnAllOrders() {
        Order order = Order.builder()
                .userId(1L)
                .status(OrderStatus.CREATED)
                .totalPrice(new BigDecimal("30.00"))
                .deleted(false)
                .build();
        order.setId(10L);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> orderPage = new PageImpl<>(List.of(order), pageable, 1);

        when(orderRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(orderPage);
        when(userServiceClient.getUsersByIds(anyCollection())).thenReturn(List.of(userInfo));

        OrderResponseDto orderResponseDto = new OrderResponseDto(
                10L, 1L, OrderStatus.CREATED, new BigDecimal("30.00"),
                List.of(), null, null
        );

        when(orderMapper.toResponseDto(order)).thenReturn(orderResponseDto);

        Page<OrderWithUserResponseDto> result = orderService.getAll(null, null, null, pageable);

        assertEquals(orderResponseDto, result.getContent().getFirst().order());
        assertEquals(userInfo, result.getContent().getFirst().user());

        verify(orderRepository).findAll(any(Specification.class), eq(pageable));
        verify(userServiceClient).getUsersByIds(Set.of(1L));
        verify(userServiceClient, never()).getUserById(any());
        verify(orderMapper).toResponseDto(order);
    }

    @Test
    @DisplayName("get all - batch users by ids")
    void getAllShouldCallGetUsersByIdsOnceForDifferentUsers() {
        Order order1 = Order.builder()
                .userId(1L)
                .status(OrderStatus.CREATED)
                .totalPrice(new BigDecimal("30.00"))
                .deleted(false)
                .build();
        order1.setId(10L);

        Order order2 = Order.builder()
                .userId(2L)
                .status(OrderStatus.CREATED)
                .totalPrice(new BigDecimal("50.00"))
                .deleted(false)
                .build();
        order2.setId(11L);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> orderPage = new PageImpl<>(List.of(order1, order2), pageable, 2);

        UserInfoDto userInfo2 = new UserInfoDto(
                2L, "Anna", "Petrova", "anna.mail@gmail.com",
                LocalDate.of(2001, Month.MAY, 1), true,
                LocalDateTime.now(), LocalDateTime.now()
        );

        when(orderRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(orderPage);
        when(userServiceClient.getUsersByIds(anyCollection()))
                .thenReturn(List.of(userInfo, userInfo2));

        OrderResponseDto dto1 = new OrderResponseDto(
                10L, 1L, OrderStatus.CREATED, new BigDecimal("30.00"),
                List.of(), null, null
        );
        OrderResponseDto dto2 = new OrderResponseDto(
                11L, 2L, OrderStatus.CREATED, new BigDecimal("50.00"),
                List.of(), null, null
        );
        when(orderMapper.toResponseDto(order1)).thenReturn(dto1);
        when(orderMapper.toResponseDto(order2)).thenReturn(dto2);

        Page<OrderWithUserResponseDto> result = orderService.getAll(null, null, null, pageable);

        assertEquals(2, result.getContent().size());
        assertEquals(userInfo, result.getContent().get(0).user());
        assertEquals(userInfo2, result.getContent().get(1).user());

        verify(userServiceClient, times(1)).getUsersByIds(Set.of(1L, 2L));
        verify(userServiceClient, never()).getUserById(any());
    }

    @Test
    @DisplayName("get by user id - success")
    void getByUserIdShouldReturnOrderWithUser() {
        Order order = Order.builder()
                .userId(1L)
                .status(OrderStatus.CREATED)
                .totalPrice(new BigDecimal("30.00"))
                .deleted(false)
                .build();
        order.setId(10L);

        when(orderRepository.findByUserIdAndDeletedFalse(1L)).thenReturn(List.of(order));
        when(userServiceClient.getUserById(1L)).thenReturn(userInfo);

        OrderResponseDto orderResponseDto = new OrderResponseDto(
                10L, 1L, OrderStatus.CREATED, new BigDecimal("30.00"),
                List.of(), null, null
        );
        when(orderMapper.toResponseDto(order)).thenReturn(orderResponseDto);

        List<OrderWithUserResponseDto> result = orderService.getByUserId(1L);

        assertEquals(orderResponseDto, result.getFirst().order());
        assertEquals(userInfo, result.getFirst().user());

        verify(orderRepository).findByUserIdAndDeletedFalse(1L);
        verify(userServiceClient).getUserById(1L);
        verify(orderMapper).toResponseDto(order);
    }

    @Test
    @DisplayName("get by user id - AccessDeniedException")
    void getByUserIdShouldThrowAccessDeniedException() {
        assertThrows(AccessDeniedException.class, () -> orderService.getByUserId(2L));

        verify(orderRepository, never()).findByUserIdAndDeletedFalse(any());
        verify(userServiceClient, never()).getUserById(any());
        verify(orderMapper, never()).toResponseDto(any());
    }

    @Test
    @DisplayName("update - success")
    void updateShouldReturnUpdatedOrderWithUser() {
        Order order = Order.builder()
                .userId(1L)
                .status(OrderStatus.CREATED)
                .totalPrice(new BigDecimal("30.00"))
                .deleted(false)
                .build();
        order.setId(10L);

        OrderUpdateRequestDto orderUpdateRequestDto = new OrderUpdateRequestDto(OrderStatus.CREATED);

        when(orderRepository.findByIdAndDeletedFalse(10L)).thenReturn(Optional.of(order));
        when(userServiceClient.getUserById(1L)).thenReturn(userInfo);

        OrderResponseDto orderResponseDto = new OrderResponseDto(
                10L, 1L, OrderStatus.CREATED, new BigDecimal("30.00"),
                List.of(), null, null
        );

        when(orderMapper.toResponseDto(order)).thenReturn(orderResponseDto);

        OrderWithUserResponseDto result = orderService.update(10L, orderUpdateRequestDto);

        assertEquals(orderResponseDto, result.order());
        assertEquals(userInfo, result.user());

        verify(orderRepository).findByIdAndDeletedFalse(10L);
        verify(orderMapper).updateEntity(orderUpdateRequestDto, order);
        verify(userServiceClient).getUserById(1L);
        verify(orderMapper).toResponseDto(order);
    }

    @Test
    @DisplayName("update - EntityNotFoundException")
    void updateShouldThrowEntityNotFoundException() {
        OrderUpdateRequestDto updateDto = new OrderUpdateRequestDto(OrderStatus.CONFIRMED);

        when(orderRepository.findByIdAndDeletedFalse(10L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> orderService.update(10L, updateDto));

        assertEquals("Order not found 10", exception.getMessage());

        verify(orderRepository).findByIdAndDeletedFalse(10L);
        verify(orderMapper, never()).updateEntity(any(), any());
        verify(userServiceClient, never()).getUserById(any());
        verify(orderMapper, never()).toResponseDto(any());
    }

    @Test
    @DisplayName("update - AccessDeniedException")
    void updateShouldThrowAccessDeniedException() {
        Order order = Order.builder()
                .userId(2L)
                .status(OrderStatus.CREATED)
                .totalPrice(new BigDecimal("30.00"))
                .deleted(false)
                .build();
        order.setId(10L);

        OrderUpdateRequestDto updateDto = new OrderUpdateRequestDto(OrderStatus.CONFIRMED);

        when(orderRepository.findByIdAndDeletedFalse(10L)).thenReturn(Optional.of(order));

        assertThrows(AccessDeniedException.class,
                () -> orderService.update(10L, updateDto));

        verify(orderRepository).findByIdAndDeletedFalse(10L);
        verify(orderMapper, never()).updateEntity(any(), any());
        verify(userServiceClient, never()).getUserById(any());
        verify(orderMapper, never()).toResponseDto(any());
    }

    @Test
    @DisplayName("updateStatusFromPayment -  confirmed")
    void updateStatusFromPaymentWhenSuccessShouldConfirmOrder() {
        Order order = Order.builder()
                .userId(1L)
                .status(OrderStatus.CREATED)
                .deleted(false)
                .build();
        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

        orderService.updateStatusFromPayment(10L, "SUCCESS");

        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
    }

    @Test
    @DisplayName("updateStatusFromPayment - cancelled")
    void updateStatusFromPaymentWhenFailedShouldCancelOrder() {
        Order order = Order.builder()
                .userId(1L)
                .status(OrderStatus.CREATED)
                .deleted(false)
                .build();

        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));
        orderService.updateStatusFromPayment(10L, "FAILED");

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
    }

    @Test
    @DisplayName("updateStatusFromPayment - do not change status")
    void updateStatusFromPaymentWhenNotCreatedShouldNotChangeStatus() {
        Order order = Order.builder()
                .userId(1L)
                .status(OrderStatus.CONFIRMED)
                .deleted(false)
                .build();

        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));
        orderService.updateStatusFromPayment(10L, "SUCCESS");

        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
    }

    @Test
    @DisplayName("updateStatusFromPayment - FAILED does not overwrite CONFIRMED")
    void updateStatusFromPaymentWhenConfirmedShouldIgnoreFailed() {
        Order order = Order.builder()
                .userId(1L)
                .status(OrderStatus.CONFIRMED)
                .deleted(false)
                .build();

        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));
        orderService.updateStatusFromPayment(10L, "FAILED");

        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
    }

    @Test
    @DisplayName("updateStatusFromPayment - SUCCESS after CANCELLED confirms order")
    void updateStatusFromPaymentWhenCancelledAndSuccessShouldConfirm() {
        Order order = Order.builder()
                .userId(1L)
                .status(OrderStatus.CANCELLED)
                .deleted(false)
                .build();

        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));
        orderService.updateStatusFromPayment(10L, "SUCCESS");

        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
    }
    @Test
    @DisplayName("updateStatusFromPayment - EntityNotFoundException")
    void updateStatusFromPaymentWhenOrderMissingShouldThrow() {
        when(orderRepository.findById(10L)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> orderService.updateStatusFromPayment(10L, "SUCCESS"));

        assertEquals("Order not found 10", ex.getMessage());
    }

    @Test
    @DisplayName("delete - success")
    void deleteShouldSoftDeleteOrder() {
        Order order = Order.builder()
                .userId(1L)
                .status(OrderStatus.CREATED)
                .totalPrice(new BigDecimal("30.00"))
                .deleted(false)
                .build();
        order.setId(10L);

        when(orderRepository.findByIdAndDeletedFalse(10L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        orderService.delete(10L);

        assertTrue(order.getDeleted());

        verify(orderRepository).findByIdAndDeletedFalse(10L);
        verify(orderRepository).save(order);
    }

    @Test
    @DisplayName("delete - EntityNotFoundException")
    void deleteShouldThrowEntityNotFoundException() {
        when(orderRepository.findByIdAndDeletedFalse(10L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> orderService.delete(10L));

        assertEquals("Order not found 10", exception.getMessage());

        verify(orderRepository).findByIdAndDeletedFalse(10L);
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("delete - AccessDeniedException")
    void deleteShouldThrowAccessDeniedException() {

        Order order = Order.builder()
                .userId(2L)
                .status(OrderStatus.CREATED)
                .totalPrice(new BigDecimal("30.00"))
                .deleted(false)
                .build();
        order.setId(10L);

        when(orderRepository.findByIdAndDeletedFalse(10L)).thenReturn(Optional.of(order));

        assertThrows(AccessDeniedException.class, () -> orderService.delete(10L));

        verify(orderRepository).findByIdAndDeletedFalse(10L);
        verify(orderRepository, never()).save(any());
    }
}
