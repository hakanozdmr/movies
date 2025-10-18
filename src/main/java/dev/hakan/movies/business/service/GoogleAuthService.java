package dev.hakan.movies.business.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import dev.hakan.movies.data.dto.AuthResponse;
import dev.hakan.movies.data.model.User;
import dev.hakan.movies.data.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;
import java.util.logging.Logger;

@Service
public class GoogleAuthService {

    private static final Logger logger = Logger.getLogger(GoogleAuthService.class.getName());
    private final UserRepository userRepository;
    private final String googleClientId;

    @Autowired
    public GoogleAuthService(UserRepository userRepository, 
                           @Value("${google.client.id}") String googleClientId) {
        this.userRepository = userRepository;
        this.googleClientId = googleClientId;
    }

    public AuthResponse authenticateWithGoogle(String idToken) {
        try {
            logger.info("Starting Google authentication with client ID: " + 
                       (googleClientId != null && !googleClientId.trim().isEmpty() ? "configured" : "not configured"));
            
            // Validate input parameters
            if (idToken == null || idToken.trim().isEmpty()) {
                logger.warning("ID token is null or empty");
                return AuthResponse.error("ID token is required");
            }
            
            if (googleClientId == null || googleClientId.trim().isEmpty()) {
                logger.severe("Google client ID is not configured");
                return AuthResponse.error("Google client ID is not configured");
            }
            
            // Check if using test client ID
            if (googleClientId.contains("test-client-id")) {
                logger.warning("Using TEST Google Client ID: " + googleClientId);
                return AuthResponse.error("Test Google Client ID detected. Please configure actual Client ID from Google Cloud Console.");
            }

            JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), 
                    jsonFactory
            )
            .setAudience(Collections.singletonList(googleClientId))
            .build();

            GoogleIdToken googleIdToken = verifier.verify(idToken);
            
            if (googleIdToken != null) {
                GoogleIdToken.Payload payload = googleIdToken.getPayload();
                String googleId = payload.getSubject();
                String email = payload.getEmail();
                String name = (String) payload.get("name");
                String picture = (String) payload.get("picture");

                // Google ID ile kullanıcı var mı kontrol et
                User existingUser = userRepository.findByGoogleId(googleId).orElse(null);
                
                if (existingUser != null) {
                    // Mevcut kullanıcı giriş yapıyor
                    existingUser.setLastLoginAt(LocalDateTime.now());
                    userRepository.save(existingUser);
                    
                    String token = UUID.randomUUID().toString();
                    return AuthResponse.success(token, existingUser.getId().toString(), 
                                              existingUser.getEmail(), existingUser.getName());
                } else {
                    // Email ile kullanıcı var mı kontrol et
                    User userWithEmail = userRepository.findByEmail(email).orElse(null);
                    
                    if (userWithEmail != null) {
                        // Email var ama Google ID yok, Google ID ekle
                        userWithEmail.setGoogleId(googleId);
                        userWithEmail.setPicture(picture);
                        userWithEmail.setProvider("google");
                        userWithEmail.setLastLoginAt(LocalDateTime.now());
                        userRepository.save(userWithEmail);
                        
                        String token = UUID.randomUUID().toString();
                        return AuthResponse.success(token, userWithEmail.getId().toString(), 
                                                  userWithEmail.getEmail(), userWithEmail.getName());
                    } else {
                        // Yeni kullanıcı oluştur
                        User newUser = new User();
                        newUser.setGoogleId(googleId);
                        newUser.setEmail(email);
                        newUser.setName(name);
                        newUser.setPicture(picture);
                        newUser.setProvider("google");
                        newUser.setActive(true);
                        newUser.setCreatedAt(LocalDateTime.now());
                        newUser.setLastLoginAt(LocalDateTime.now());
                        
                        User savedUser = userRepository.save(newUser);
                        
                        String token = UUID.randomUUID().toString();
                        return AuthResponse.success(token, savedUser.getId().toString(), 
                                                  savedUser.getEmail(), savedUser.getName());
                    }
                }
            } else {
                logger.warning("Google ID token verification failed - token is null");
                return AuthResponse.error("Invalid Google ID token");
            }
            
        } catch (GeneralSecurityException | IOException e) {
            logger.severe("Google authentication failed: " + e.getMessage());
            return AuthResponse.error("Google authentication failed: " + e.getMessage());
        } catch (Exception e) {
            logger.severe("Unexpected error during Google authentication: " + e.getMessage());
            return AuthResponse.error("An unexpected error occurred during Google authentication: " + e.getMessage());
        }
    }
}
