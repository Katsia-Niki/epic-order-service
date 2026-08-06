package by.nikiforova.epic_order_service.security;

import by.nikiforova.epic_order_service.service.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private FilterChain filterChain;

    @Mock
    private Claims claims;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldContinueWhenAuthorizationHeaderMissing() throws Exception {
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService);
    }

    @Test
    void shouldContinueWhenTokenInvalid() throws Exception {
        request.addHeader("Authorization", "Bearer bad-token");
        when(jwtService.validateToken("bad-token")).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verify(filterChain).doFilter(request, response);
        verify(jwtService).validateToken("bad-token");
        verify(jwtService, never()).parseToken(any());
    }

    @Test
    void shouldSetAuthenticationWhenTokenValid() throws Exception {
        request.addHeader("Authorization", "Bearer valid-token");

        when(jwtService.validateToken("valid-token")).thenReturn(true);
        when(jwtService.parseToken("valid-token")).thenReturn(claims);
        when(claims.get("userId", Long.class)).thenReturn(1L);
        when(claims.getSubject()).thenReturn("Ivan");
        when(claims.get("role", String.class)).thenReturn("USER");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("Ivan", SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        assertEquals(1L, SecurityContextHolder.getContext().getAuthentication().getDetails());
        assertTrue(SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> Objects.equals(a.getAuthority(), "ROLE_USER")));
    }
}
