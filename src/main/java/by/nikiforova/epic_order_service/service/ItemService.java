package by.nikiforova.epic_order_service.service;

import by.nikiforova.epic_order_service.dto.request.ItemRequestDto;
import by.nikiforova.epic_order_service.dto.response.ItemResponseDto;
import by.nikiforova.epic_order_service.entity.Item;
import by.nikiforova.epic_order_service.exception.EntityNotFoundException;
import by.nikiforova.epic_order_service.mapper.ItemMapper;
import by.nikiforova.epic_order_service.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static by.nikiforova.epic_order_service.constant.Constants.ITEM_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;

    @Transactional
    public ItemResponseDto create(ItemRequestDto dto) {
        Item item = itemMapper.toEntity(dto);
        Item saved = itemRepository.save(item);
        return itemMapper.toResponseDto(saved);
    }

    @Transactional(readOnly = true)
    public ItemResponseDto getById(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ITEM_NOT_FOUND + id));
        return itemMapper.toResponseDto(item);
    }

    @Transactional(readOnly = true)
    public List<ItemResponseDto> getAll() {
        List<Item> items = itemRepository.findAll();
        List<ItemResponseDto> result = new ArrayList<>();
        for (Item item : items) {
            result.add(itemMapper.toResponseDto(item));
        }
        return result;
    }

    @Transactional
    public ItemResponseDto update(Long id, ItemRequestDto dto) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ITEM_NOT_FOUND + id));

        itemMapper.updateEntity(dto, item);
        return itemMapper.toResponseDto(item);
    }

    @Transactional
    public void delete(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ITEM_NOT_FOUND + id));
        itemRepository.delete(item);
    }
}
