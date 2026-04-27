package hsd.inflab.smp.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Testklasse für den PasswordService.
 *
 * Ziel:
 * - Überprüfen, ob die Passwort-Hash-Verifizierung korrekt funktioniert.
 * - Sicherstellen, dass die Methode verifyPassword die erwarteten Ergebnisse liefert.
 *
 * @ExtendWith(MockitoExtension.class) ermöglicht die Verwendung von Mockito in den Tests.
 */
@ExtendWith(MockitoExtension.class)
class PasswordServiceTest {

    /**
     * InjectMocks initialisiert das zu testende Objekt (PasswordService)
     * und injiziert die gemockten Abhängigkeiten.
     */
    @InjectMocks
    private PasswordService pwService;

    /**
     * Testet, ob verifyPassword true zurückgibt, wenn der Hash mit dem Passwort übereinstimmt.
     *
     * Erwartung:
     * - Für gültige Passwort-Hash-Kombinationen wird true zurückgegeben.
     */
    @Test
    void verifyPassword_whenHashMatches_returnsTrue() {
        // Testfälle mit gültigen Passwort-Hash-Kombinationen
        assertTrue(
                pwService.verifyPassword("null", "74234e98afe7498fb5daf1f36ac2d78acc339464f950703b8c019892f982b90b"));
        assertTrue(
                pwService.verifyPassword("123456", "8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92"));
        assertTrue(pwService.verifyPassword(
                "password", "5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8"));
        assertTrue(pwService.verifyPassword("", "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"));
        assertTrue(pwService.verifyPassword(
                "!!//=?;:@€", "5449700f38c5ea4aa7378208d5bb0d40068d98560f27f63dc87526637a553f03"));
    }

    /**
     * Testet, ob verifyPassword false zurückgibt, wenn der Hash nicht mit dem Passwort übereinstimmt.
     *
     * Erwartung:
     * - Für ungültige Passwort-Hash-Kombinationen wird false zurückgegeben.
     */
    @Test
    void verifyPassword_whenHashDoesNotMatch_returnsFalse() {
        // Testfälle mit ungültigen Passwort-Hash-Kombinationen
        assertFalse(
                pwService.verifyPassword("null", "74234e98afe7498fb5daf1f36ac2d78456339464f950703b8c019892f982b90b"));
        assertFalse(
                pwService.verifyPassword("123456", "8d969eef6ecad3c29a3a629280e686fc0c3f5d5a86aff3ca12020c923adc6c92"));
        assertFalse(pwService.verifyPassword("password", ""));
        assertFalse(pwService.verifyPassword("", "e3b0c44298fc1c149afbf4ae41e4649b934ca495991b7852b855"));
        assertFalse(pwService.verifyPassword(
                "!!//=?;:@€", "5449700F38c5ea4aa7378208d5bb0d40068d98560f27f63dc87526637a553f03"));
    }
}
