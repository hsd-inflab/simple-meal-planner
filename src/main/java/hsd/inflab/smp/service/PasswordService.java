package hsd.inflab.smp.service;

import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.util.HexFormat;

@Service
public class PasswordService {

    private String hash(String password) {
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(password.getBytes())
            );
        } catch (Exception e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    public boolean verifyPassword(String inputPassword, String storedPasswordHash) {
        return hash(inputPassword).equals(storedPasswordHash);
    }
}