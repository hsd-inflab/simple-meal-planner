package hsd.inflab.smp.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordService {
    private final PasswordEncoder passwordEncoder;

    public PasswordService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public boolean verifyPassword(String inputPassword, String storedPasswordHash) {
        return passwordEncoder.matches(inputPassword, storedPasswordHash);
    }

    public String hashPassword(String password) {
        return passwordEncoder.encode(password);
    }
}
