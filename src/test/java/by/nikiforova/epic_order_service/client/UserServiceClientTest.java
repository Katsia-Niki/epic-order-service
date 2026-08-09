package by.nikiforova.epic_order_service.client;

import by.nikiforova.epic_order_service.dto.response.UserInfoDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import static by.nikiforova.epic_order_service.constant.Constants.PLACEHOLDER_EMAIL;
import static by.nikiforova.epic_order_service.constant.Constants.PLACEHOLDER_NAME;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class UserServiceClientTest {

    private final UserServiceClient client = new UserServiceClient();

    @Test
    @DisplayName("getUsersByIds - empty when ids null or empty")
    void getUsersByIdsShouldReturnEmptyWhenIdsNullOrEmpty() {
        assertEquals(List.of(), client.getUsersByIds(null));
        assertEquals(List.of(), client.getUsersByIds(List.of()));
    }

    @Test
    @DisplayName("fallback getUserById - returns placeholder, does not throw")
    void getUserByIdFallbackShouldReturnPlaceholder() throws Exception {
        Method fallback = UserServiceClient.class.getDeclaredMethod(
                "getUserByIdFallback", Long.class, Throwable.class);
        fallback.setAccessible(true);

        UserInfoDto result = assertDoesNotThrow(
                () -> (UserInfoDto) fallback.invoke(client, 7L, new RuntimeException("down")));

        assertEquals(7L, result.id());
        assertEquals(PLACEHOLDER_NAME, result.name());
        assertEquals(PLACEHOLDER_NAME, result.surname());
        assertEquals(PLACEHOLDER_EMAIL, result.email());
    }

    @Test
    @DisplayName("fallback getUserByEmail - returns placeholder, does not throw")
    void getUserByEmailFallbackShouldReturnPlaceholder() throws Exception {
        Method fallback = UserServiceClient.class.getDeclaredMethod(
                "getUserByEmailFallback", String.class, Throwable.class);
        fallback.setAccessible(true);

        UserInfoDto result = assertDoesNotThrow(
                () -> (UserInfoDto) fallback.invoke(
                        client, "ivan@mail.com", new RuntimeException("down")));

        assertNull(result.id());
        assertEquals(PLACEHOLDER_NAME, result.name());
        assertEquals(PLACEHOLDER_EMAIL, result.email());
    }

    @Test
    @DisplayName("fallback getUsersByIds - returns placeholders, does not throw")
    void getUsersByIdsFallbackShouldReturnPlaceholders() throws Exception {
        Method fallback = UserServiceClient.class.getDeclaredMethod(
                "getUsersByIdsFallback", Collection.class, Throwable.class);
        fallback.setAccessible(true);

        @SuppressWarnings("unchecked")
        List<UserInfoDto> result = assertDoesNotThrow(
                () -> (List<UserInfoDto>) fallback.invoke(
                        client, List.of(1L, 2L), new RuntimeException("down")));

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).id());
        assertEquals(2L, result.get(1).id());
        assertEquals(PLACEHOLDER_EMAIL, result.get(0).email());
        assertEquals(PLACEHOLDER_NAME, result.get(0).name());
    }
}
