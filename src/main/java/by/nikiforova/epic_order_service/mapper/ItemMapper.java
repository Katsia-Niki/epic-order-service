package by.nikiforova.epic_order_service.mapper;

import by.nikiforova.epic_order_service.dto.request.ItemRequestDto;
import by.nikiforova.epic_order_service.dto.response.ItemResponseDto;
import by.nikiforova.epic_order_service.entity.Item;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ItemMapper {

    Item toEntity(ItemRequestDto dto);

    ItemResponseDto toResponseDto(Item entity);

    void updateEntity(ItemRequestDto dto, @MappingTarget Item item);
}
