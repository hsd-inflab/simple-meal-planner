package hsd.inflab.smp.config;

import hsd.inflab.smp.entity.User;
import hsd.inflab.smp.enums.Role;
import hsd.inflab.smp.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@ConditionalOnProperty(prefix = "app.security.seed-default-user", name = "enabled", havingValue = "true")
public class DataLoader {

    @Value("${app.security.user.name}")
    private String defaultUserName;

    @Value("${app.security.user.password}")
    private String defaultUserPassword;

    @Bean
    public CommandLineRunner createDefaultUser(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            Optional<User> existing = userRepository.findByUsername(defaultUserName);
            if (existing.isEmpty()) {
                User u = new User(defaultUserName, passwordEncoder.encode(defaultUserPassword), List.of(Role.USER));
                userRepository.save(u);
            }
        };
    }
}
