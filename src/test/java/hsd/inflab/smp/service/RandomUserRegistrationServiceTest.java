package hsd.inflab.smp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import hsd.inflab.smp.dto.response.RandomRegistrationResponse;
import hsd.inflab.smp.entity.User;
import hsd.inflab.smp.enums.Role;
import hsd.inflab.smp.repository.UserRepository;
import hsd.inflab.smp.security.JwtService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

class RandomUserRegistrationServiceTest {

    @Test
    void registerRandomUser_generatesCredentialsHashesPasswordAndSavesUser() {
        UniqueUsernameService uniqueUsernameService = org.mockito.Mockito.mock(UniqueUsernameService.class);
        RandomPasswordGenerator randomPasswordGenerator = org.mockito.Mockito.mock(RandomPasswordGenerator.class);
        PasswordEncoder passwordEncoder = org.mockito.Mockito.mock(PasswordEncoder.class);
        UserRepository userRepository = org.mockito.Mockito.mock(UserRepository.class);
        JwtService jwtService = org.mockito.Mockito.mock(JwtService.class);
        RandomUserRegistrationService service = new RandomUserRegistrationService(
                uniqueUsernameService, randomPasswordGenerator, passwordEncoder, userRepository, jwtService);

        when(uniqueUsernameService.findAvailableUsername()).thenReturn("CalmFalcon99");
        when(randomPasswordGenerator.generatePassword()).thenReturn("Falcon42!");
        when(passwordEncoder.encode("Falcon42!")).thenReturn("hashed-password");
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("jwt-token");

        RandomRegistrationResponse response = service.registerRandomUser();

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getUsername()).isEqualTo("CalmFalcon99");
        assertThat(savedUser.getPasswordHash()).isEqualTo("hashed-password");
        assertThat(savedUser.getRoles()).containsExactly(Role.USER);
        assertThat(response.username()).isEqualTo("CalmFalcon99");
        assertThat(response.password()).isEqualTo("Falcon42!");
    }

    @Test
    void registerRandomUser_returnsBearerTokenForSavedUser() {
        UniqueUsernameService uniqueUsernameService = org.mockito.Mockito.mock(UniqueUsernameService.class);
        RandomPasswordGenerator randomPasswordGenerator = org.mockito.Mockito.mock(RandomPasswordGenerator.class);
        PasswordEncoder passwordEncoder = org.mockito.Mockito.mock(PasswordEncoder.class);
        UserRepository userRepository = org.mockito.Mockito.mock(UserRepository.class);
        JwtService jwtService = org.mockito.Mockito.mock(JwtService.class);
        RandomUserRegistrationService service = new RandomUserRegistrationService(
                uniqueUsernameService, randomPasswordGenerator, passwordEncoder, userRepository, jwtService);

        when(uniqueUsernameService.findAvailableUsername()).thenReturn("CalmFalcon99");
        when(randomPasswordGenerator.generatePassword()).thenReturn("Falcon42!");
        when(passwordEncoder.encode("Falcon42!")).thenReturn("hashed-password");
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("jwt-token");

        RandomRegistrationResponse response = service.registerRandomUser();

        ArgumentCaptor<UserDetails> userDetailsCaptor = ArgumentCaptor.forClass(UserDetails.class);
        verify(jwtService).generateToken(userDetailsCaptor.capture());
        UserDetails tokenUser = userDetailsCaptor.getValue();
        assertThat(tokenUser.getUsername()).isEqualTo("CalmFalcon99");
        assertThat(tokenUser.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_USER");
        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.type()).isEqualTo("Bearer");
    }
}
