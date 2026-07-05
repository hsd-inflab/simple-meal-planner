package hsd.inflab.smp.controller;

import hsd.inflab.smp.dto.request.LoginRequest;
import hsd.inflab.smp.dto.response.LoginResponse;
import hsd.inflab.smp.dto.response.RandomRegistrationResponse;
import hsd.inflab.smp.service.AuthService;
import hsd.inflab.smp.service.RandomUserRegistrationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final RandomUserRegistrationService randomUserRegistrationService;

    public AuthController(AuthService authService, RandomUserRegistrationService randomUserRegistrationService) {
        this.authService = authService;
        this.randomUserRegistrationService = randomUserRegistrationService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        try {
            return ResponseEntity.ok(authService.login(request));
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/register/random")
    public ResponseEntity<RandomRegistrationResponse> registerRandomUser() {
        return ResponseEntity.status(HttpStatus.CREATED).body(randomUserRegistrationService.registerRandomUser());
    }
}
