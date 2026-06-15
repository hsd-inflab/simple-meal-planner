package hsd.inflab.smp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import hsd.inflab.smp.entity.User;
import hsd.inflab.smp.enums.Role;
import hsd.inflab.smp.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @Test
    void loadUserByUsername_withNullRoles_returnsUserWithoutAuthorities() {
        User user = new User("admin", "encoded", null);
        CustomUserDetailsService service = new CustomUserDetailsService(userRepository);
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));

        UserDetails result = service.loadUserByUsername("admin");

        assertEquals("admin", result.getUsername());
        assertEquals("encoded", result.getPassword());
        assertTrue(result.getAuthorities().isEmpty());
    }

    @Test
    void loadUserByUsername_withRoles_mapsAuthorities() {
        User user = new User("admin", "encoded", List.of(Role.USER, Role.ADMIN));
        CustomUserDetailsService service = new CustomUserDetailsService(userRepository);
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));

        UserDetails result = service.loadUserByUsername("admin");

        assertTrue(result.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_USER")));
        assertTrue(result.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN")));
    }
}
