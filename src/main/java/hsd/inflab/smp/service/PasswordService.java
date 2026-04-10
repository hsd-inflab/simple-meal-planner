package hsd.inflab.smp.service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import org.springframework.stereotype.Service;

@Service
public class PasswordService {

    private String hash(String password) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(password.getBytes()));
        } catch (NoSuchAlgorithmException e) { // Wenn der Algorithmus "SHA-256" nicht verfügbar ist....
            throw new IllegalArgumentException("Failed hashing password", e);
        }
    }

    public boolean verifyPassword(String inputPassword, String storedPasswordHash) {
        return hash(inputPassword).equals(storedPasswordHash);
    }
}
