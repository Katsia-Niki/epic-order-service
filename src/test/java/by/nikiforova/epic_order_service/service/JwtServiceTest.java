package by.nikiforova.epic_order_service.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String SECRET = "test-secret-key-test-secret-key-test-secret-key";
    private static final Long USER_ID = 1L;
    private static final String ROLE = "USER";
    private static final String LOGIN = "liza";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET);
    }

    @Test
    @DisplayName("validateToken - success")
    void validateTokenWhenTokenValidShouldReturnTrue() {
        String token = createToken();

        assertTrue(jwtService.validateToken(token));
    }

    @Test
    @DisplayName("validateToken - invalid token")
    void validateTokenWhenTokenInvalidShouldReturnFalse() {

        assertFalse(jwtService.validateToken("not-a-token"));
    }

    @Test
    @DisplayName("parseToken - success")
    void parseTokenWhenTokenValidShouldReturnClaims() {
        String token = createToken();

        Claims claims = jwtService.parseToken(token);

        assertEquals(USER_ID, claims.get("userId", Long.class));
        assertEquals(ROLE, claims.get("role", String.class));
        assertEquals(LOGIN, claims.getSubject());
    }

    private String createToken() {
        return Jwts.builder()
                .subject(LOGIN)
                .claim("userId", USER_ID)
                .claim("role", ROLE)
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }
}
