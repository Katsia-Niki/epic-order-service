package by.nikiforova.epic_order_service.mapper;

import by.nikiforova.epic_order_service.dto.request.OrderUpdateRequestDto;
import by.nikiforova.epic_order_service.dto.response.OrderItemResponseDto;
import by.nikiforova.epic_order_service.dto.response.OrderResponseDto;
import by.nikiforova.epic_order_service.entity.Order;
import by.nikiforova.epic_order_service.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(source = "item.id", target = "itemId")
    @Mapping(source = "item.name", target = "name")
    @Mapping(source = "item.price", target = "price")
    OrderItemResponseDto toItemResponseDto(OrderItem orderItem);

    OrderResponseDto toResponseDto (Order entity);

    void updateEntity(OrderUpdateRequestDto dto, @MappingTarget Order order);

    default Page<OrderResponseDto> toDtoPage(Page<Order> orderPage) {

        return orderPage.map(this::toResponseDto);
    }

}
