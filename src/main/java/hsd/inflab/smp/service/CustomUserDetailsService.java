package hsd.inflab.smp.service;

import hsd.inflab.smp.repository.UserRepository;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        var user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        var authorities = Optional.ofNullable(user.getRoles()).orElseGet(java.util.List::of).stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .collect(Collectors.toList());

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(), user.getPasswordHash(), authorities);
    }
}
