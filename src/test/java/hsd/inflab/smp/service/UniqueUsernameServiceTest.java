package hsd.inflab.smp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import hsd.inflab.smp.repository.UserRepository;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class UniqueUsernameServiceTest {

    @Test
    void findAvailableUsername_returnsGeneratedUsernameWhenItDoesNotExist() {
        UsernameGeneratorStub usernameGenerator = new UsernameGeneratorStub("CalmFalcon99");
        UserRepositoryMock userRepository = new UserRepositoryMock();
        UniqueUsernameService service = new UniqueUsernameService(usernameGenerator, userRepository.repository());

        userRepository.usernameExists("CalmFalcon99", false);

        String username = service.findAvailableUsername();

        assertThat(username).isEqualTo("CalmFalcon99");
        userRepository.verifyChecked("CalmFalcon99");
    }

    @Test
    void findAvailableUsername_regeneratesUsernameWhenGeneratedUsernameAlreadyExists() {
        UsernameGeneratorStub usernameGenerator = new UsernameGeneratorStub("BraveTiger07", "CalmFalcon99");
        UserRepositoryMock userRepository = new UserRepositoryMock();
        UniqueUsernameService service = new UniqueUsernameService(usernameGenerator, userRepository.repository());

        userRepository.usernameExists("BraveTiger07", true);
        userRepository.usernameExists("CalmFalcon99", false);

        String username = service.findAvailableUsername();

        assertThat(username).isEqualTo("CalmFalcon99");
        userRepository.verifyChecked("BraveTiger07");
        userRepository.verifyChecked("CalmFalcon99");
    }

    @Test
    void findAvailableUsername_throwsExceptionWhenNoUsernameCanBeFound() {
        UsernameGeneratorStub usernameGenerator = new UsernameGeneratorStub("BraveTiger07");
        UserRepositoryMock userRepository = new UserRepositoryMock();
        UniqueUsernameService service = new UniqueUsernameService(usernameGenerator, userRepository.repository());

        userRepository.usernameExists("BraveTiger07", true);

        assertThatThrownBy(service::findAvailableUsername)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No available username could be generated.");
        userRepository.verifyChecked("BraveTiger07", 20);
    }

    private static final class UsernameGeneratorStub implements UsernameGenerator {

        private final Queue<String> usernames;
        private String lastUsername;

        private UsernameGeneratorStub(String... usernames) {
            this.usernames = new ArrayDeque<>(List.of(usernames));
        }

        @Override
        public String generateUsername() {
            if (usernames.peek() != null) {
                lastUsername = usernames.remove();
            }
            return lastUsername;
        }
    }

    private static final class UserRepositoryMock {

        private final UserRepository repository = Mockito.mock(UserRepository.class);

        private UserRepository repository() {
            return repository;
        }

        private void usernameExists(String username, boolean exists) {
            when(repository.existsByUsername(username)).thenReturn(exists);
        }

        private void verifyChecked(String username) {
            verify(repository).existsByUsername(username);
        }

        private void verifyChecked(String username, int times) {
            verify(repository, times(times)).existsByUsername(username);
        }
    }
}
