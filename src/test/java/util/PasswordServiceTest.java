package util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import services.PasswordService;

public class PasswordServiceTest {
    PasswordService pwService;

    @BeforeEach
    public void setUp() {
        pwService = new PasswordService();
    }

    @Test
    public void testVerifyPassword_Correct() {
        assertTrue(pwService.verifyPassword("null", "74234e98afe7498fb5daf1f36ac2d78acc339464f950703b8c019892f982b90b"));
        assertTrue(pwService.verifyPassword("123456", "8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92"));
        assertTrue(pwService.verifyPassword("password", "5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8"));
        assertTrue(pwService.verifyPassword("", "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"));
        assertTrue(pwService.verifyPassword("!!//=?;:@€", "5449700f38c5ea4aa7378208d5bb0d40068d98560f27f63dc87526637a553f03"));
    }

    @Test
    public void testVerifyPassword_Incorrect() {
        assertFalse(pwService.verifyPassword("null", "74234e98afe7498fb5daf1f36ac2d78456339464f950703b8c019892f982b90b"));       //numbers instead of letter
        assertFalse(pwService.verifyPassword("123456", "8d969eef6ecad3c29a3a629280e686fc0c3f5d5a86aff3ca12020c923adc6c92"));     //different char
        assertFalse(pwService.verifyPassword("password", ""));                                                                   //no hash
        assertFalse(pwService.verifyPassword("", "e3b0c44298fc1c149afbf4ae41e4649b934ca495991b7852b855"));                       //shorter Hash
        assertFalse(pwService.verifyPassword("!!//=?;:@€", "5449700F38c5ea4aa7378208d5bb0d40068d98560f27f63dc87526637a553f03")); //Uppercase
    }
}
