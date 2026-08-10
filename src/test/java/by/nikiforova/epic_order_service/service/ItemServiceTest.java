package by.nikiforova.epic_order_service.service;

import by.nikiforova.epic_order_service.dto.request.ItemRequestDto;
import by.nikiforova.epic_order_service.dto.response.ItemResponseDto;
import by.nikiforova.epic_order_service.entity.Item;
import by.nikiforova.epic_order_service.exception.EntityNotFoundException;
import by.nikiforova.epic_order_service.mapper.ItemMapper;
import by.nikiforova.epic_order_service.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;
    @Mock
    private ItemMapper itemMapper;

    @InjectMocks
    private ItemService itemService;

    private Item item;
    private ItemRequestDto itemRequestDto;
    private ItemResponseDto itemResponseDto;

    @BeforeEach
    void setUp() {
        item = Item.builder()
                .name("Socks")
                .price(new BigDecimal("10.00"))
                .build();
        item.setId(5L);

        itemRequestDto = new ItemRequestDto("Socks", new BigDecimal("10.00"));
        itemResponseDto = new ItemResponseDto(5L, "Socks", new BigDecimal("10.00"), null, null);
    }

    @Test
    @DisplayName("add item - success")
    void addItemSuccess() {
        when(itemMapper.toEntity(itemRequestDto)).thenReturn(item);
        when(itemRepository.save(item)).thenReturn(item);
        when(itemMapper.toResponseDto(item)).thenReturn(itemResponseDto);

        ItemResponseDto result = itemService.create(itemRequestDto);

        assertEquals(itemResponseDto, result);

        verify(itemMapper).toEntity(itemRequestDto);
        verify(itemRepository).save(item);
        verify(itemMapper).toResponseDto(item);
    }

    @Test
    @DisplayName("get by id - success")
    void getByIdSuccess() {
        when(itemRepository.findById(5L)).thenReturn(Optional.of(item));
        when(itemMapper.toResponseDto(item)).thenReturn(itemResponseDto);

        ItemResponseDto result = itemService.getById(5L);

        assertEquals(itemResponseDto, result);

        verify(itemRepository).findById(5L);
        verify(itemMapper).toResponseDto(item);
    }

    @Test
    @DisplayName("get by id - EntityNotFoundException")
    void getByIdShouldThrowEntityNotFoundException() {
        when(itemRepository.findById(5L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> itemService.getById(5L));

        assertEquals("Item not found 5", exception.getMessage());

        verify(itemRepository).findById(5L);
        verify(itemMapper, never()).toResponseDto(any());
    }

    @Test
    @DisplayName("get all - success")
    void getAllSuccess() {
        when(itemRepository.findAll()).thenReturn(List.of(item));
        when(itemMapper.toResponseDto(item)).thenReturn(itemResponseDto);

        List<ItemResponseDto> result = itemService.getAll();

        assertEquals(1, result.size());
        assertEquals(itemResponseDto, result.getFirst());

        verify(itemRepository).findAll();
        verify(itemMapper).toResponseDto(item);
    }

    @Test
    @DisplayName("update item - success")
    void updateItemSuccess() {
        when(itemRepository.findById(5L)).thenReturn(Optional.of(item));
        when(itemMapper.toResponseDto(item)).thenReturn(itemResponseDto);

        ItemResponseDto result = itemService.update(5L, itemRequestDto);

        assertEquals(itemResponseDto, result);

        verify(itemRepository).findById(5L);
        verify(itemMapper).updateEntity(itemRequestDto, item);
        verify(itemMapper).toResponseDto(item);
    }

    @Test
    @DisplayName("update item - EntityNotFoundException")
    void updateShouldThrowEntityNotFoundException() {
        when(itemRepository.findById(5L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> itemService.update(5L, itemRequestDto));

        assertEquals("Item not found 5", exception.getMessage());

        verify(itemRepository).findById(5L);
        verify(itemMapper, never()).updateEntity(any(), any());
        verify(itemMapper, never()).toResponseDto(any());
    }

    @Test
    @DisplayName("delete item - success")
    void deleteItemSuccess() {
        when(itemRepository.findById(5L)).thenReturn(Optional.of(item));

        itemService.delete(5L);

        verify(itemRepository).findById(5L);
        verify(itemRepository).delete(item);
    }

    @Test
    @DisplayName("delete item - EntityNotFoundException")
    void deleteShouldThrowEntityNotFoundException() {
        when(itemRepository.findById(5L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> itemService.delete(5L));

        assertEquals("Item not found 5", exception.getMessage());

        verify(itemRepository).findById(5L);
        verify(itemRepository, never()).delete(any());
    }
}
