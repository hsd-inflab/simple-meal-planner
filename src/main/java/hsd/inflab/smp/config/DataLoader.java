package hsd.inflab.smp.config;

import hsd.inflab.smp.entity.User;
import hsd.inflab.smp.repository.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataLoader {

    @Value("${app.security.user.name:admin}")
    private String defaultUserName;

    @Value("${app.security.user.password:secret}")
    private String defaultUserPassword;

    @Bean
    public CommandLineRunner createDefaultUser(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            Optional<User> existing = userRepository.findByUsername(defaultUserName);
            if (existing.isEmpty()) {
                User u = new User(
                        UUID.randomUUID(), defaultUserName, passwordEncoder.encode(defaultUserPassword), "USER");
                userRepository.save(u);
            }
        };
    }
}
