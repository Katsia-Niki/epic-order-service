package by.nikiforova.epic_order_service.service;

import by.nikiforova.epic_order_service.client.UserServiceClient;
import by.nikiforova.epic_order_service.dto.request.OrderCreateRequestDto;
import by.nikiforova.epic_order_service.dto.request.OrderItemRequestDto;
import by.nikiforova.epic_order_service.dto.request.OrderUpdateRequestDto;
import by.nikiforova.epic_order_service.dto.response.OrderWithUserResponseDto;
import by.nikiforova.epic_order_service.dto.response.UserInfoDto;
import by.nikiforova.epic_order_service.entity.Item;
import by.nikiforova.epic_order_service.entity.Order;
import by.nikiforova.epic_order_service.entity.OrderItem;
import by.nikiforova.epic_order_service.entity.OrderStatus;
import by.nikiforova.epic_order_service.exception.EntityNotFoundException;
import by.nikiforova.epic_order_service.mapper.OrderMapper;
import by.nikiforova.epic_order_service.repository.ItemRepository;
import by.nikiforova.epic_order_service.repository.OrderRepository;
import by.nikiforova.epic_order_service.specification.OrderSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private static final String ORDER_NOT_FOUND = "Order not found ";

    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;
    private final UserServiceClient userServiceClient;
    private final OrderMapper  orderMapper;

    @Transactional
    public OrderWithUserResponseDto createOrder(OrderCreateRequestDto dto) {

        log.info("Starting order creation: email={}", dto.email());

        UserInfoDto userInfo = userServiceClient.getUserByEmail(dto.email());

        Order newOrder = Order.builder()
                .userId(userInfo.id())
                .status(OrderStatus.CREATED)
                .deleted(false)
                .build();

        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequestDto incoming : dto.orderItems()) {
            Item item = itemRepository.findById(incoming.itemId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Item not found " + incoming.itemId()));

            OrderItem orderItem = OrderItem.builder()
                    .order(newOrder)
                    .item(item)
                    .quantity(incoming.quantity())
                    .build();

            newOrder.getOrderItems().add(orderItem);
            total = total.add(item.getPrice().multiply(BigDecimal.valueOf(incoming.quantity())));
        }

        newOrder.setTotalPrice(total);
        Order order = orderRepository.save(newOrder);
        return new OrderWithUserResponseDto(orderMapper.toResponseDto(order), userInfo);
    }

    @Transactional(readOnly = true)
    public OrderWithUserResponseDto getById(Long orderId) {

        Order order = orderRepository.findByIdAndDeletedFalse(orderId)
                .orElseThrow(() -> new EntityNotFoundException(ORDER_NOT_FOUND + orderId));

        return toOrderWithUserResponseDto(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderWithUserResponseDto> getAll(OrderStatus status, LocalDateTime createdFrom, LocalDateTime createdTo, Pageable pageable) {

        Specification<Order> spec = Specification.where(OrderSpecification.notDeleted())
                .and(OrderSpecification.createdFrom(createdFrom))
                .and(OrderSpecification.createdTo(createdTo))
                .and(OrderSpecification.hasStatus(status));

        Page<Order> orderPage = orderRepository.findAll(spec, pageable);

        return orderPage.map(this::toOrderWithUserResponseDto);
    }

    @Transactional(readOnly = true)
    public List<OrderWithUserResponseDto> getByUserId(Long userId) {

        List<Order> orders = orderRepository.findByUserIdAndDeletedFalse(userId);
        UserInfoDto userInfo = userServiceClient.getUserById(userId);

        List<OrderWithUserResponseDto> result = new ArrayList<>();

        for (Order order : orders) {
            result.add(new OrderWithUserResponseDto(
                    orderMapper.toResponseDto(order),
                    userInfo
            ));
        }
        return result;
    }

    @Transactional
    public OrderWithUserResponseDto update(Long orderId, OrderUpdateRequestDto dto) {
        Order orderToUpdate = orderRepository.findByIdAndDeletedFalse(orderId)
                .orElseThrow(() -> new EntityNotFoundException(ORDER_NOT_FOUND + orderId));

        orderMapper.updateEntity(dto, orderToUpdate);

        return toOrderWithUserResponseDto(orderToUpdate);
    }

    @Transactional
    public void delete(Long orderId) {
        Order orderToDelete = orderRepository.findByIdAndDeletedFalse(orderId)
                .orElseThrow(() -> new EntityNotFoundException(ORDER_NOT_FOUND + orderId));
        orderToDelete.setDeleted(true);
        orderRepository.save(orderToDelete);
    }

    private OrderWithUserResponseDto toOrderWithUserResponseDto(Order order) {
        UserInfoDto userInfo = userServiceClient.getUserById(order.getUserId());
        return new OrderWithUserResponseDto(orderMapper.toResponseDto(order), userInfo);
    }
}
