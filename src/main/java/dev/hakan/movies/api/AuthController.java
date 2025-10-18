package dev.hakan.movies.api;

import dev.hakan.movies.business.service.AuthService;
import dev.hakan.movies.business.service.GoogleAuthService;
import dev.hakan.movies.data.dto.AuthResponse;
import dev.hakan.movies.data.dto.GoogleAuthRequest;
import dev.hakan.movies.data.dto.LoginRequest;
import dev.hakan.movies.data.dto.RegisterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private final AuthService authService;
    private final GoogleAuthService googleAuthService;

    @Autowired
    public AuthController(AuthService authService, GoogleAuthService googleAuthService) {
        this.authService = authService;
        this.googleAuthService = googleAuthService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @PostMapping("/google")
    public ResponseEntity<AuthResponse> loginWithGoogle(@Valid @RequestBody GoogleAuthRequest request) {
        try {
            AuthResponse response = googleAuthService.authenticateWithGoogle(request.getIdToken());
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                // Return 400 Bad Request for validation errors, 401 for auth failures
                if (response.getMessage().contains("required") || response.getMessage().contains("configured")) {
                    return ResponseEntity.badRequest().body(response);
                } else {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
                }
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(AuthResponse.error("Google authentication failed: " + e.getMessage()));
        }
    }
}
