package by.nikiforova.epic_order_service.util;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {

    }

    public static Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || auth.getDetails() == null) {
            throw new AccessDeniedException("Unauthenticated");
        }
        return (Long) auth.getDetails();
    }

    public static boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return false;
        }
        return auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

    public static void checkAccess(Long userId) {
        if (isAdmin()) {
            return;
        }
        if (!userId.equals(currentUserId())) {
            throw new AccessDeniedException("Access denied");
        }
    }
}
