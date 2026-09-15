package hsd.inflab.smp.service;

import hsd.inflab.smp.dto.response.RandomRegistrationResponse;
import hsd.inflab.smp.entity.User;
import hsd.inflab.smp.enums.Role;
import hsd.inflab.smp.repository.UserRepository;
import hsd.inflab.smp.security.JwtService;
import java.util.List;
import java.util.Optional;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RandomUserRegistrationService {

    private static final String TOKEN_TYPE = "Bearer";

    private final UniqueUsernameService uniqueUsernameService;
    private final RandomPasswordGenerator randomPasswordGenerator;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final Optional<UserStarterDataService> userStarterDataService;

    public RandomUserRegistrationService(
            UniqueUsernameService uniqueUsernameService,
            RandomPasswordGenerator randomPasswordGenerator,
            PasswordEncoder passwordEncoder,
            UserRepository userRepository,
            JwtService jwtService,
            Optional<UserStarterDataService> userStarterDataService) {
        this.uniqueUsernameService = uniqueUsernameService;
        this.randomPasswordGenerator = randomPasswordGenerator;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.userStarterDataService = userStarterDataService;
    }

    @Transactional
    public RandomRegistrationResponse registerRandomUser() {
        return registerRandomUser(List.of(Role.USER));
    }

    @Transactional
    public RandomRegistrationResponse registerRandomUser(List<Role> roles) {
        String username = uniqueUsernameService.findAvailableUsername();
        String password = randomPasswordGenerator.generatePassword();
        String passwordHash = passwordEncoder.encode(password);
        User savedUser = userRepository.save(new User(username, passwordHash, roles));
        userStarterDataService.ifPresent(starterData -> starterData.createStarterDataFor(savedUser));
        String token = jwtService.generateToken(userDetails(username, passwordHash));
        return new RandomRegistrationResponse(username, password, token, TOKEN_TYPE);
    }

    private UserDetails userDetails(String username, String passwordHash) {
        return new org.springframework.security.core.userdetails.User(
                username, passwordHash, List.of(new SimpleGrantedAuthority("ROLE_" + Role.USER.name())));
    }
}
