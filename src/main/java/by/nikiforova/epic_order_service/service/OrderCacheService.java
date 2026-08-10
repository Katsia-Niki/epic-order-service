package by.nikiforova.epic_order_service.service;

import by.nikiforova.epic_order_service.client.UserServiceClient;
import by.nikiforova.epic_order_service.dto.response.OrderWithUserResponseDto;
import by.nikiforova.epic_order_service.dto.response.UserInfoDto;
import by.nikiforova.epic_order_service.entity.Order;
import by.nikiforova.epic_order_service.exception.EntityNotFoundException;
import by.nikiforova.epic_order_service.mapper.OrderMapper;
import by.nikiforova.epic_order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static by.nikiforova.epic_order_service.constant.Constants.ORDERS_CACHE;
import static by.nikiforova.epic_order_service.constant.Constants.ORDER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class OrderCacheService {
    private final OrderRepository orderRepository;
    private final UserServiceClient userServiceClient;
    private final OrderMapper orderMapper;

    @Cacheable(value = ORDERS_CACHE, key = "#orderId")
    @Transactional(readOnly = true)
    public OrderWithUserResponseDto getById(Long orderId) {
        Order order = orderRepository.findByIdAndDeletedFalse(orderId)
                .orElseThrow(() -> new EntityNotFoundException(ORDER_NOT_FOUND + orderId));
        UserInfoDto userInfo = userServiceClient.getUserById(order.getUserId());
        return new OrderWithUserResponseDto(orderMapper.toResponseDto(order), userInfo);
    }
    @CacheEvict(value = ORDERS_CACHE, key = "#orderId")
    public void evict(Long orderId) {

    }
}
