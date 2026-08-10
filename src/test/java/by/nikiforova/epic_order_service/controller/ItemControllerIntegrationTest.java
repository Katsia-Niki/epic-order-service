package by.nikiforova.epic_order_service.controller;

import by.nikiforova.epic_order_service.dto.request.ItemRequestDto;
import by.nikiforova.epic_order_service.entity.Item;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ItemControllerIntegrationTest extends AbstractIntegrationTest {

    @Test
    void shouldCreateItemAndSaveInDatabase() throws Exception {
        ItemRequestDto requestDto = new ItemRequestDto("Socks", new BigDecimal("10.00"));

        mockMvc.perform(post("/api/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Socks"))
                .andExpect(jsonPath("$.price").value(10.00));

        assertThat(itemRepository.count()).isEqualTo(1);

        Item savedItem = itemRepository.findAll().getFirst();

        assertThat(savedItem.getName()).isEqualTo("Socks");
        assertThat(savedItem.getPrice()).isEqualByComparingTo("10.00");
    }

    @Test
    void shouldGetItemById()  throws Exception {
        Item saved = itemRepository.save(Item.builder()
                .name("Socks")
                .price(new BigDecimal("10.00"))
                .build());

        mockMvc.perform(get("/api/items/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId().intValue()))
                .andExpect(jsonPath("$.name").value("Socks"))
                .andExpect(jsonPath("$.price").value(10.00));
    }

    @Test
    void shouldGetAllItems()  throws Exception {
        itemRepository.save(Item.builder()
                .name("Socks")
                .price(new BigDecimal("10.00"))
                .build());

        itemRepository.save(Item.builder()
                .name("Dress")
                .price(new BigDecimal("50.00"))
                .build());

        mockMvc.perform(get("/api/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").exists())
                .andExpect(jsonPath("$[1].name").exists());
    }
}
