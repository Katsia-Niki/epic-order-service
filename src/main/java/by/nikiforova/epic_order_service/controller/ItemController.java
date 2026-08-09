package by.nikiforova.epic_order_service.controller;

import by.nikiforova.epic_order_service.dto.request.ItemRequestDto;
import by.nikiforova.epic_order_service.dto.response.ItemResponseDto;
import by.nikiforova.epic_order_service.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @GetMapping
    public ResponseEntity<List<ItemResponseDto>> getAllItems() {
        List<ItemResponseDto> response = itemService.getAll();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ItemResponseDto> addItem(@Valid @RequestBody ItemRequestDto itemRequestDto) {
        ItemResponseDto response = itemService.create(itemRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemResponseDto> getItemById(@PathVariable Long id) {
        ItemResponseDto response = itemService.getById(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ItemResponseDto> updateItem(@PathVariable Long id, @Valid @RequestBody ItemRequestDto itemRequestDto) {
        ItemResponseDto response = itemService.update(id, itemRequestDto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        itemService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
