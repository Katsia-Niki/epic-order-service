package by.nikiforova.epic_order_service.client;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserServiceClientTest {

    private final UserServiceClient client = new UserServiceClient();

    @Test
    @DisplayName("getUsersByIds - empty when ids null or empty")
    void getUsersByIdsShouldReturnEmptyWhenIdsNullOrEmpty() {
        assertEquals(List.of(), client.getUsersByIds(null));
        assertEquals(List.of(), client.getUsersByIds(List.of()));
    }
}
