package hsd.inflab.smp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import hsd.inflab.smp.dto.response.RandomRegistrationResponse;
import hsd.inflab.smp.entity.User;
import hsd.inflab.smp.enums.Role;
import hsd.inflab.smp.repository.UserRepository;
import hsd.inflab.smp.security.JwtService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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
        UserStarterDataService userStarterDataService = org.mockito.Mockito.mock(UserStarterDataService.class);
        RandomUserRegistrationService service = new RandomUserRegistrationService(
                uniqueUsernameService,
                randomPasswordGenerator,
                passwordEncoder,
                userRepository,
                jwtService,
                Optional.of(userStarterDataService));

        when(uniqueUsernameService.findAvailableUsername()).thenReturn("CalmFalcon99");
        when(randomPasswordGenerator.generatePassword()).thenReturn("Falcon42!");
        when(passwordEncoder.encode("Falcon42!")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
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
        UserStarterDataService userStarterDataService = org.mockito.Mockito.mock(UserStarterDataService.class);
        RandomUserRegistrationService service = new RandomUserRegistrationService(
                uniqueUsernameService,
                randomPasswordGenerator,
                passwordEncoder,
                userRepository,
                jwtService,
                Optional.of(userStarterDataService));

        when(uniqueUsernameService.findAvailableUsername()).thenReturn("CalmFalcon99");
        when(randomPasswordGenerator.generatePassword()).thenReturn("Falcon42!");
        when(passwordEncoder.encode("Falcon42!")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("jwt-token");

        RandomRegistrationResponse response = service.registerRandomUser();

        ArgumentCaptor<UserDetails> userDetailsCaptor = ArgumentCaptor.forClass(UserDetails.class);
        verify(jwtService).generateToken(userDetailsCaptor.capture());
        assertThat(userDetailsCaptor.getValue().getUsername()).isEqualTo("CalmFalcon99");
        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.type()).isEqualTo("Bearer");
    }

    @Test
    void registerRandomUser_createsStarterDataForThePersistedUser() {
        UniqueUsernameService uniqueUsernameService = org.mockito.Mockito.mock(UniqueUsernameService.class);
        RandomPasswordGenerator randomPasswordGenerator = org.mockito.Mockito.mock(RandomPasswordGenerator.class);
        PasswordEncoder passwordEncoder = org.mockito.Mockito.mock(PasswordEncoder.class);
        UserRepository userRepository = org.mockito.Mockito.mock(UserRepository.class);
        JwtService jwtService = org.mockito.Mockito.mock(JwtService.class);
        UserStarterDataService userStarterDataService = org.mockito.Mockito.mock(UserStarterDataService.class);
        RandomUserRegistrationService service = new RandomUserRegistrationService(
                uniqueUsernameService,
                randomPasswordGenerator,
                passwordEncoder,
                userRepository,
                jwtService,
                Optional.of(userStarterDataService));

        User persistedUser = new User("CalmFalcon99", "hashed-password", java.util.List.of(Role.USER));
        when(uniqueUsernameService.findAvailableUsername()).thenReturn("CalmFalcon99");
        when(randomPasswordGenerator.generatePassword()).thenReturn("Falcon42!");
        when(passwordEncoder.encode("Falcon42!")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenReturn(persistedUser);
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("jwt-token");

        service.registerRandomUser();

        // The starter data must be attached to the entity returned by save(), because only that one carries the id.
        verify(userStarterDataService).createStarterDataFor(persistedUser);
    }

    @Test
    void registerRandomUser_propagatesFailure_whenStarterDataCreationFails() {
        UniqueUsernameService uniqueUsernameService = org.mockito.Mockito.mock(UniqueUsernameService.class);
        RandomPasswordGenerator randomPasswordGenerator = org.mockito.Mockito.mock(RandomPasswordGenerator.class);
        PasswordEncoder passwordEncoder = org.mockito.Mockito.mock(PasswordEncoder.class);
        UserRepository userRepository = org.mockito.Mockito.mock(UserRepository.class);
        JwtService jwtService = org.mockito.Mockito.mock(JwtService.class);
        UserStarterDataService userStarterDataService = org.mockito.Mockito.mock(UserStarterDataService.class);
        RandomUserRegistrationService service = new RandomUserRegistrationService(
                uniqueUsernameService,
                randomPasswordGenerator,
                passwordEncoder,
                userRepository,
                jwtService,
                Optional.of(userStarterDataService));

        when(uniqueUsernameService.findAvailableUsername()).thenReturn("CalmFalcon99");
        when(randomPasswordGenerator.generatePassword()).thenReturn("Falcon42!");
        when(passwordEncoder.encode("Falcon42!")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        org.mockito.Mockito.doThrow(new IllegalStateException("starter data failed"))
                .when(userStarterDataService)
                .createStarterDataFor(any(User.class));

        // The failure must not be swallowed: only a propagating exception lets the transaction roll back,
        // so no account is left behind without its starter data.
        assertThatThrownBy(service::registerRandomUser)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("starter data failed");
        verifyNoInteractions(jwtService);
    }

    @Test
    void registerRandomUser_skipsStarterData_whenServiceIsAbsent() {
        UniqueUsernameService uniqueUsernameService = org.mockito.Mockito.mock(UniqueUsernameService.class);
        RandomPasswordGenerator randomPasswordGenerator = org.mockito.Mockito.mock(RandomPasswordGenerator.class);
        PasswordEncoder passwordEncoder = org.mockito.Mockito.mock(PasswordEncoder.class);
        UserRepository userRepository = org.mockito.Mockito.mock(UserRepository.class);
        JwtService jwtService = org.mockito.Mockito.mock(JwtService.class);
        RandomUserRegistrationService service = new RandomUserRegistrationService(
                uniqueUsernameService,
                randomPasswordGenerator,
                passwordEncoder,
                userRepository,
                jwtService,
                Optional.empty());

        when(uniqueUsernameService.findAvailableUsername()).thenReturn("CalmFalcon99");
        when(randomPasswordGenerator.generatePassword()).thenReturn("Falcon42!");
        when(passwordEncoder.encode("Falcon42!")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("jwt-token");

        // The account must still be created; only the starter data is skipped.
        RandomRegistrationResponse response = service.registerRandomUser();

        assertThat(response.username()).isEqualTo("CalmFalcon99");
        assertThat(response.token()).isEqualTo("jwt-token");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerRandomUser_persistsTheRequestedRoles() {
        UniqueUsernameService uniqueUsernameService = org.mockito.Mockito.mock(UniqueUsernameService.class);
        RandomPasswordGenerator randomPasswordGenerator = org.mockito.Mockito.mock(RandomPasswordGenerator.class);
        PasswordEncoder passwordEncoder = org.mockito.Mockito.mock(PasswordEncoder.class);
        UserRepository userRepository = org.mockito.Mockito.mock(UserRepository.class);
        JwtService jwtService = org.mockito.Mockito.mock(JwtService.class);
        UserStarterDataService userStarterDataService = org.mockito.Mockito.mock(UserStarterDataService.class);
        RandomUserRegistrationService service = new RandomUserRegistrationService(
                uniqueUsernameService,
                randomPasswordGenerator,
                passwordEncoder,
                userRepository,
                jwtService,
                Optional.of(userStarterDataService));

        when(uniqueUsernameService.findAvailableUsername()).thenReturn("CalmFalcon99");
        when(randomPasswordGenerator.generatePassword()).thenReturn("Falcon42!");
        when(passwordEncoder.encode("Falcon42!")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("jwt-token");

        service.registerRandomUser(java.util.List.of(Role.TESTUSER));

        // The role decides later behaviour, for example whether starter data is created at all.
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getRoles()).containsExactly(Role.TESTUSER);
    }

    @Test
    void registerRandomUser_withoutArguments_persistsTheDefaultUserRole() {
        UniqueUsernameService uniqueUsernameService = org.mockito.Mockito.mock(UniqueUsernameService.class);
        RandomPasswordGenerator randomPasswordGenerator = org.mockito.Mockito.mock(RandomPasswordGenerator.class);
        PasswordEncoder passwordEncoder = org.mockito.Mockito.mock(PasswordEncoder.class);
        UserRepository userRepository = org.mockito.Mockito.mock(UserRepository.class);
        JwtService jwtService = org.mockito.Mockito.mock(JwtService.class);
        UserStarterDataService userStarterDataService = org.mockito.Mockito.mock(UserStarterDataService.class);
        RandomUserRegistrationService service = new RandomUserRegistrationService(
                uniqueUsernameService,
                randomPasswordGenerator,
                passwordEncoder,
                userRepository,
                jwtService,
                Optional.of(userStarterDataService));

        when(uniqueUsernameService.findAvailableUsername()).thenReturn("CalmFalcon99");
        when(randomPasswordGenerator.generatePassword()).thenReturn("Falcon42!");
        when(passwordEncoder.encode("Falcon42!")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("jwt-token");

        service.registerRandomUser();

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getRoles()).containsExactly(Role.USER);
    }
}
