package hsd.inflab.smp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import hsd.inflab.smp.dto.LoginRequest;
import hsd.inflab.smp.dto.LoginResponse;
import hsd.inflab.smp.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private Authentication authentication;

    @Test
    void login_withValidCredentials_returnsBearerTokenResponse() {
        User userDetails = new User("admin", "encoded", java.util.List.of());
        LoginRequest request = new LoginRequest("admin", "secret");
        AuthService authService = new AuthService(authenticationManager, jwtService);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("jwt-token");

        LoginResponse response = authService.login(request);

        assertEquals("jwt-token", response.token());
        assertEquals("Bearer", response.type());
        verify(authenticationManager).authenticate(new UsernamePasswordAuthenticationToken("admin", "secret"));
        verify(jwtService).generateToken(userDetails);
    }
}
