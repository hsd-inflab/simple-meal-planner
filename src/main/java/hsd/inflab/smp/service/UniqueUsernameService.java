package hsd.inflab.smp.service;

import hsd.inflab.smp.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UniqueUsernameService {

    private static final int MAX_GENERATION_ATTEMPTS = 20;

    private final UsernameGenerator usernameGenerator;
    private final UserRepository userRepository;

    public UniqueUsernameService(UsernameGenerator usernameGenerator, UserRepository userRepository) {
        this.usernameGenerator = usernameGenerator;
        this.userRepository = userRepository;
    }

    public String findAvailableUsername() {
        for (int attempt = 0; attempt < MAX_GENERATION_ATTEMPTS; attempt++) {
            String username = usernameGenerator.generateUsername();
            if (!userRepository.existsByUsername(username)) {
                return username;
            }
        }
        throw new IllegalStateException("No available username could be generated.");
    }
}
