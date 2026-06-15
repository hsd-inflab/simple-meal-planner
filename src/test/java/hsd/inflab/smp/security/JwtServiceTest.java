package hsd.inflab.smp.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.util.ReflectionTestUtils;

class JwtServiceTest {

    @Test
    void generateToken_containsUsernameAndValidatesForMatchingUser() {
        JwtService jwtService = new JwtService();
        User userDetails = new User("admin", "encoded", java.util.List.of());
        configureJwtService(jwtService, 3_600_000L);

        String token = jwtService.generateToken(userDetails);

        assertEquals("admin", jwtService.extractUsername(token));
        assertTrue(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void isTokenValid_withDifferentUser_returnsFalse() {
        JwtService jwtService = new JwtService();
        User issuingUser = new User("admin", "encoded", java.util.List.of());
        User otherUser = new User("someone-else", "encoded", java.util.List.of());
        configureJwtService(jwtService, 3_600_000L);

        String token = jwtService.generateToken(issuingUser);

        assertFalse(jwtService.isTokenValid(token, otherUser));
    }

    @Test
    void isTokenValid_withExpiredToken_returnsFalse() {
        JwtService jwtService = new JwtService();
        User userDetails = new User("admin", "encoded", java.util.List.of());
        configureJwtService(jwtService, -1L);

        String token = jwtService.generateToken(userDetails);

        assertFalse(jwtService.isTokenValid(token, userDetails));
    }

    private void configureJwtService(JwtService jwtService, long expirationMs) {
        ReflectionTestUtils.setField(jwtService, "jwtSecret", "changeitchangethissecretkeymustbeatleast32bytes!");
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", expirationMs);
        jwtService.init();
    }
}
