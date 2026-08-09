package by.nikiforova.epic_order_service.controller;

import by.nikiforova.epic_order_service.dto.request.OrderCreateRequestDto;
import by.nikiforova.epic_order_service.dto.request.OrderItemRequestDto;
import by.nikiforova.epic_order_service.dto.request.OrderUpdateRequestDto;
import by.nikiforova.epic_order_service.entity.Item;
import by.nikiforova.epic_order_service.entity.Order;
import by.nikiforova.epic_order_service.entity.OrderStatus;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrderControllerIntegrationTest extends AbstractIntegrationTest {

    @Test
    void shouldCreateOrderAndSaveInDatabase() throws Exception {
        stubUserByEmail("ivan_ivanov@gmail.com", 1L);

        Item item = itemRepository.save(Item.builder()
                .name("Socks")
                .price(new BigDecimal("10.00"))
                .build());

        OrderCreateRequestDto request = new OrderCreateRequestDto(
                "ivan_ivanov@gmail.com",
                List.of(new OrderItemRequestDto(item.getId(), 3))
        );

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        assertThat(orderRepository.count()).isEqualTo(1);
    }

    @Test
    void shouldGetOrderById() throws Exception {
        stubUserByEmail("ivan_ivanov@gmail.com", 1L);

        Item item = itemRepository.save(Item.builder()
                .name("Socks")
                .price(new BigDecimal("10.00"))
                .build());

        OrderCreateRequestDto request = new OrderCreateRequestDto(
                "ivan_ivanov@gmail.com",
                List.of(new OrderItemRequestDto(item.getId(), 3))
        );

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        Long orderId = orderRepository.findAll().getFirst().getId();

        stubUserById(1L);

        mockMvc.perform(get("/api/orders/" + orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.order.id").value(orderId.intValue()))
                .andExpect(jsonPath("$.user.id").value(1))
                .andExpect(jsonPath("$.user.email").value("ivan_ivanov@gmail.com"));
    }

    @Test
    void shouldReturn404WhenOrderNotFound() throws Exception {

        mockMvc.perform(get("/api/orders/1414"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Order not found 1414"));
    }

    @Test
    void shouldGetOrderByIdWithPlaceholderWhenUserServiceUnavailable() throws Exception {
        Order order = orderRepository.save(Order.builder()
                .userId(1L)
                .status(OrderStatus.CREATED)
                .totalPrice(new BigDecimal("30.00"))
                .deleted(false)
                .build());

        WIRE_MOCK.stubFor(WireMock.get(urlPathEqualTo("/api/users/1"))
                .willReturn(aResponse().withStatus(500)));

        mockMvc.perform(get("/api/orders/" + order.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.order.id").value(order.getId().intValue()))
                .andExpect(jsonPath("$.user.name").value("Unavailable"))
                .andExpect(jsonPath("$.user.email").value("unavailable@mail"));
    }

    @Test
    void shouldGetAllOrders() throws Exception {

        stubUserByEmail("ivan_ivanov@gmail.com", 1L);

        Item item = itemRepository.save(Item.builder()
                .name("Socks")
                .price(new BigDecimal("10.00"))
                .build());

        OrderCreateRequestDto request = new OrderCreateRequestDto(
                "ivan_ivanov@gmail.com",
                List.of(new OrderItemRequestDto(item.getId(), 3))
        );

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        Long orderId = orderRepository.findAll().getFirst().getId();

        stubUsersByIds(1L);

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].order.id").value(orderId.intValue()))
                .andExpect(jsonPath("$.content[0].user.id").value(1))
                .andExpect(jsonPath("$.content[0].user.email").value("ivan_ivanov@gmail.com"));

        WIRE_MOCK.verify(WireMock.getRequestedFor(urlPathEqualTo("/api/users/by-ids")));
    }

    @Test
    void shouldGetOrdersByUserId() throws Exception {
        stubUserByEmail("ivan_ivanov@gmail.com", 1L);

        Item item = itemRepository.save(Item.builder()
                .name("Socks")
                .price(new BigDecimal("10.00"))
                .build());

        OrderCreateRequestDto request = new OrderCreateRequestDto(
                "ivan_ivanov@gmail.com",
                List.of(new OrderItemRequestDto(item.getId(), 3))
        );

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        Long orderId = orderRepository.findAll().getFirst().getId();

        stubUserById(1L);

        mockMvc.perform(get("/api/orders/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].order.id").value(orderId.intValue()))
                .andExpect(jsonPath("$[0].user.id").value(1))
                .andExpect(jsonPath("$[0].user.email").value("ivan_ivanov@gmail.com"));
    }

    @Test
    void shouldUpdateOrder() throws Exception {
        stubUserByEmail("ivan_ivanov@gmail.com", 1L);

        Item item = itemRepository.save(Item.builder()
                .name("Socks")
                .price(new BigDecimal("10.00"))
                .build());

        OrderCreateRequestDto createRequest = new OrderCreateRequestDto(
                "ivan_ivanov@gmail.com",
                List.of(new OrderItemRequestDto(item.getId(), 3))
        );

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());

        Long orderId = orderRepository.findAll().getFirst().getId();

        stubUserById(1L);

        OrderUpdateRequestDto updateRequest = new OrderUpdateRequestDto(OrderStatus.CONFIRMED);

        mockMvc.perform(patch("/api/orders/" + orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.order.id").value(orderId.intValue()))
                .andExpect(jsonPath("$.order.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.user.id").value(1));
    }

    @Test
    void shouldDeleteOrder() throws Exception {
        stubUserByEmail("ivan_ivanov@gmail.com", 1L);

        Item item = itemRepository.save(Item.builder()
                .name("Socks")
                .price(new BigDecimal("10.00"))
                .build());

        OrderCreateRequestDto createRequest = new OrderCreateRequestDto(
                "ivan_ivanov@gmail.com",
                List.of(new OrderItemRequestDto(item.getId(), 3))
        );

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());

        Long orderId = orderRepository.findAll().getFirst().getId();

        mockMvc.perform(delete("/api/orders/" + orderId))
                .andExpect(status().isNoContent());

        assertThat(orderRepository.findById(orderId)).isPresent();
        assertThat(orderRepository.findById(orderId).get().getDeleted()).isTrue();

        assertThat(orderRepository.findByIdAndDeletedFalse(orderId)).isEmpty();
    }

    @Test
    void shouldReturn400WhenCreateOrderValidationFails() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "not-an-email",
                                  "orderItems": []
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void shouldReturn403WhenUserAccessesForeignOrder() throws Exception {
        Order order = orderRepository.save(Order.builder()
                .userId(2L)
                .status(OrderStatus.CREATED)
                .totalPrice(new BigDecimal("30.00"))
                .deleted(false)
                .build());

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        "ivan",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                );
        authentication.setDetails(1L);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        mockMvc.perform(get("/api/orders/" + order.getId()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Access denied"));
    }

    private void stubUsersByIds(Long userId) {
        WIRE_MOCK.stubFor(WireMock.get(urlPathEqualTo("/api/users/by-ids"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            [
                              {
                                "id": %d,
                                "name": "Ivan",
                                "surname": "Ivanov",
                                "email": "ivan_ivanov@gmail.com",
                                "birthDate": "1992-06-12",
                                "active": true,
                                "createdAt": "2026-01-01T10:00:00",
                                "updatedAt": "2026-01-01T10:00:00"
                              }
                            ]
                            """.formatted(userId))));
    }

    private void stubUserById(Long userId) {
        WIRE_MOCK.stubFor(WireMock.get(urlPathEqualTo("/api/users/" + userId))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "id": %d,
                                  "name": "Ivan",
                                  "surname": "Ivanov",
                                  "email": "ivan_ivanov@gmail.com",
                                  "birthDate": "1992-06-12",
                                  "active": true,
                                  "createdAt": "2026-01-01T10:00:00",
                                  "updatedAt": "2026-01-01T10:00:00"
                                }
                                """.formatted(userId))));
    }

    private void stubUserByEmail(String email, Long userId) {
        WIRE_MOCK.stubFor(WireMock.get(urlPathMatching("/api/users/email/.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "id": %d,
                                  "name": "Ivan",
                                  "surname": "Ivanov",
                                  "email": "%s",
                                  "birthDate": "1992-06-12",
                                  "active": true,
                                  "createdAt": "2026-01-01T10:00:00",
                                  "updatedAt": "2026-01-01T10:00:00"
                                }
                                """.formatted(userId, email))));
    }
}
