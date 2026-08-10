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
import by.nikiforova.epic_order_service.exception.ServiceUnavailableException;
import by.nikiforova.epic_order_service.mapper.OrderMapper;
import by.nikiforova.epic_order_service.repository.ItemRepository;
import by.nikiforova.epic_order_service.repository.OrderRepository;
import by.nikiforova.epic_order_service.specification.OrderSpecification;
import by.nikiforova.epic_order_service.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static by.nikiforova.epic_order_service.constant.Constants.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;
    private final UserServiceClient userServiceClient;
    private final OrderMapper  orderMapper;
    private final OrderCacheService orderCacheService;

    @Transactional
    public OrderWithUserResponseDto createOrder(OrderCreateRequestDto dto) {

        log.info("Starting order creation: email={}", dto.email());

        UserInfoDto userInfo = userServiceClient.getUserByEmail(dto.email());

        if (PLACEHOLDER_EMAIL.equals(userInfo.email())) {
            throw new ServiceUnavailableException("User service is unavailable");
        }

        SecurityUtils.checkAccess(userInfo.id());

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

    public OrderWithUserResponseDto getById(Long orderId) {
        OrderWithUserResponseDto result = orderCacheService.getById(orderId);
        SecurityUtils.checkAccess(result.order().userId());
        return result;
    }

    @Transactional(readOnly = true)
    public Page<OrderWithUserResponseDto> getAll(OrderStatus status, LocalDateTime createdFrom,
                                                 LocalDateTime createdTo, Pageable pageable) {

        Specification<Order> spec = Specification.where(OrderSpecification.notDeleted())
                .and(OrderSpecification.createdFrom(createdFrom))
                .and(OrderSpecification.createdTo(createdTo))
                .and(OrderSpecification.hasStatus(status));

        Page<Order> orderPage = orderRepository.findAll(spec, pageable);

        Set<Long> userIds = new HashSet<>();
        for (Order order : orderPage.getContent()) {
            userIds.add(order.getUserId());
        }

        List<UserInfoDto> users = userServiceClient.getUsersByIds(userIds);

        Map<Long, UserInfoDto> usersById = new HashMap<>();
        for (UserInfoDto user : users) {
            usersById.put(user.id(), user);
        }

        List<OrderWithUserResponseDto> result = new ArrayList<>();

        for (Order order : orderPage.getContent()) {
            result.add(new OrderWithUserResponseDto(orderMapper.toResponseDto(order),
                    usersById.get(order.getUserId())));
        }
        return new PageImpl<>(result, pageable, orderPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public List<OrderWithUserResponseDto> getByUserId(Long userId) {

        SecurityUtils.checkAccess(userId);

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

    @CacheEvict(value = ORDERS_CACHE, key = "#orderId")
    @Transactional
    public OrderWithUserResponseDto update(Long orderId, OrderUpdateRequestDto dto) {
        Order orderToUpdate = orderRepository.findByIdAndDeletedFalse(orderId)
                .orElseThrow(() -> new EntityNotFoundException(ORDER_NOT_FOUND + orderId));

        SecurityUtils.checkAccess(orderToUpdate.getUserId());

        orderMapper.updateEntity(dto, orderToUpdate);

        return toOrderWithUserResponseDto(orderToUpdate);
    }

    @CacheEvict(value = ORDERS_CACHE, key = "#orderId")
    @Transactional
    public void delete(Long orderId) {
        Order orderToDelete = orderRepository.findByIdAndDeletedFalse(orderId)
                .orElseThrow(() -> new EntityNotFoundException(ORDER_NOT_FOUND + orderId));

        SecurityUtils.checkAccess(orderToDelete.getUserId());

        orderToDelete.setDeleted(true);
        orderRepository.save(orderToDelete);
    }

    private OrderWithUserResponseDto toOrderWithUserResponseDto(Order order) {
        UserInfoDto userInfo = userServiceClient.getUserById(order.getUserId());
        return new OrderWithUserResponseDto(orderMapper.toResponseDto(order), userInfo);
    }
}
