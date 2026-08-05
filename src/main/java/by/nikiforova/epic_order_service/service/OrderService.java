package by.nikiforova.epic_order_service.service;

import by.nikiforova.epic_order_service.entity.Item;
import by.nikiforova.epic_order_service.entity.Order;
import by.nikiforova.epic_order_service.entity.OrderItem;
import by.nikiforova.epic_order_service.entity.OrderStatus;
import by.nikiforova.epic_order_service.exception.EntityNotFoundException;
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
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private static final String ORDER_NOT_FOUND = "Order not found ";

    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;

    @Transactional
    public Order createOrder(Order order) {

        log.info("Starting order creation: email={}", order.getId());

        Order newOrder = Order.builder()
                .userId(order.getUserId())
                .status(OrderStatus.CREATED)
                .deleted(false)
                .build();

        BigDecimal total = BigDecimal.ZERO;

        for (OrderItem incoming : order.getOrderItems()) {
            Item item = itemRepository.findById(incoming.getItem().getId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Item not found " + incoming.getItem().getId()));

            OrderItem orderItem = OrderItem.builder()
                    .order(newOrder)
                    .item(item)
                    .quantity(incoming.getQuantity())
                    .build();

            newOrder.getOrderItems().add(orderItem);
            total = total.add(item.getPrice().multiply(BigDecimal.valueOf(incoming.getQuantity())));
        }

        newOrder.setTotalPrice(total);
        return orderRepository.save(newOrder);
    }

    @Transactional(readOnly = true)
    public Order getById(Long orderId) {

        return orderRepository.findByIdAndDeletedFalse(orderId)
                .orElseThrow(() -> new EntityNotFoundException(ORDER_NOT_FOUND + orderId));
    }

    @Transactional(readOnly = true)
    public Page<Order> getAll(OrderStatus status, LocalDateTime createdFrom, LocalDateTime createdTo, Pageable pageable) {

        Specification<Order> spec = Specification.where(OrderSpecification.notDeleted())
                .and(OrderSpecification.createdFrom(createdFrom))
                .and(OrderSpecification.createdTo(createdTo))
                .and(OrderSpecification.hasStatus(status));

        return orderRepository.findAll(spec, pageable);
    }

    @Transactional(readOnly = true)
    public List<Order> getByUserId(Long userId) {
        return orderRepository.findByUserIdAndDeletedFalse(userId);
    }

    @Transactional
    public Order update(Order order) {
        Order orderToUpdate = orderRepository.findByIdAndDeletedFalse(order.getId())
                .orElseThrow(() -> new EntityNotFoundException(ORDER_NOT_FOUND + order.getId()));
        orderToUpdate.setStatus(order.getStatus());
        return orderRepository.save(orderToUpdate);
    }

    @Transactional
    public void delete(Long orderId) {
        Order orderToDelete = orderRepository.findByIdAndDeletedFalse(orderId)
                .orElseThrow(() -> new EntityNotFoundException(ORDER_NOT_FOUND + orderId));
        orderToDelete.setDeleted(true);
        orderRepository.save(orderToDelete);
    }
}
