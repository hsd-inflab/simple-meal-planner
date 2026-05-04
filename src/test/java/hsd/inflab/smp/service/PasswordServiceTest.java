package hsd.inflab.smp.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class PasswordServiceTest {
    private final PasswordService passwordService = new PasswordService(new BCryptPasswordEncoder());

    @Test
    void hashPassword_returnsBcryptHash() {
        String passwordHash = passwordService.hashPassword("secret");

        assertTrue(passwordHash.startsWith("$2"));
        assertTrue(passwordService.verifyPassword("secret", passwordHash));
    }

    @Test
    void verifyPassword_whenPasswordDoesNotMatch_returnsFalse() {
        String passwordHash = passwordService.hashPassword("secret");

        assertFalse(passwordService.verifyPassword("wrong", passwordHash));
    }
}
