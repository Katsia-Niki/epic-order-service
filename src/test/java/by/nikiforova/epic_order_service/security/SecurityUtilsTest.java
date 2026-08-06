package by.nikiforova.epic_order_service.security;

import by.nikiforova.epic_order_service.util.SecurityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SecurityUtilsTest {

    private static final String ROLE_USER = "ROLE_USER";
    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("currentUserId - success")
    void currentUserIdWhenAuthenticatedShouldReturnUserId() {
        setAuthentication(1L, ROLE_USER);

        assertEquals(1L, SecurityUtils.currentUserId());
    }

    @Test
    @DisplayName("currentUserId - AccessDeniedException")
    void currentUserIdWhenUnauthenticatedShouldThrowAccessDeniedException() {
        assertThrows(AccessDeniedException.class, SecurityUtils::currentUserId);
    }

    @Test
    @DisplayName("isAdmin - true")
    void isAdminWhenRoleAdminShouldReturnTrue() {
        setAuthentication(1L, ROLE_ADMIN);

        assertTrue(SecurityUtils.isAdmin());
    }

    @Test
    @DisplayName("isAdmin - false")
    void isAdminWhenRoleUserShouldReturnFalse() {
        setAuthentication(1L, ROLE_USER);

        assertFalse(SecurityUtils.isAdmin());
    }

    @Test
    @DisplayName("checkAccess - admin can access any user")
    void checkAccessWhenAdminShouldAllowAnyUserId() {
        setAuthentication(1L, ROLE_ADMIN);

        assertDoesNotThrow(() -> SecurityUtils.checkAccess(99L));
    }

    @Test
    @DisplayName("checkAccess - success")
    void checkAccessWhenSameUserShouldAllow() {
        setAuthentication(1L, ROLE_USER);

        assertDoesNotThrow(() -> SecurityUtils.checkAccess(1L));
    }

    @Test
    @DisplayName("checkAccess - AccessDeniedException")
    void checkAccessWhenOtherUserShouldThrowAccessDeniedException() {
        setAuthentication(1L, ROLE_USER);

        assertThrows(AccessDeniedException.class, () -> SecurityUtils.checkAccess(2L));
    }

    private void setAuthentication(Long userId, String role) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        "liza",
                        null,
                        List.of(new SimpleGrantedAuthority(role))
                );
        authentication.setDetails(userId);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
