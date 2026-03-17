package services;

import java.security.MessageDigest;
import java.util.HexFormat;

/**
 * provides password utilties
 */
public class PasswordService {

    private static String hash(String password) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest((password.getBytes())));
        } catch (Exception e) { // NOPMD
            System.out.println("Error hashing password" + e.getMessage()); // NOPMD
        }
        return null;
    }

    public boolean verifyPassword(String inputPassword, String storedPasswordHash) {
        return hash(inputPassword).equals(storedPasswordHash); // Placeholder implementation
    }
}
