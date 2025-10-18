package dev.hakan.movies.business.service;

import dev.hakan.movies.data.dto.AuthResponse;
import dev.hakan.movies.data.dto.LoginRequest;
import dev.hakan.movies.data.dto.RegisterRequest;
import dev.hakan.movies.data.model.User;
import dev.hakan.movies.data.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponse register(RegisterRequest request) {
        try {
            // Email kontrolü
            if (userRepository.existsByEmail(request.getEmail())) {
                return AuthResponse.error("Email already exists");
            }

            // Yeni kullanıcı oluştur
            User user = new User();
            user.setName(request.getName());
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setActive(true);

            User savedUser = userRepository.save(user);

            // Token oluştur (basit UUID)
            String token = UUID.randomUUID().toString();

            return AuthResponse.success(token, savedUser.getId().toString(), savedUser.getEmail(), savedUser.getName());
        } catch (Exception e) {
            return AuthResponse.error("Registration failed: " + e.getMessage());
        }
    }

    public AuthResponse login(LoginRequest request) {
        try {
            User user = userRepository.findByEmail(request.getEmail())
                    .orElse(null);

            if (user == null || !user.isActive()) {
                return AuthResponse.error("Invalid email or password");
            }

            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                return AuthResponse.error("Invalid email or password");
            }

            // Son giriş tarihini güncelle
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);

            // Token oluştur
            String token = UUID.randomUUID().toString();

            return AuthResponse.success(token, user.getId().toString(), user.getEmail(), user.getName());
        } catch (Exception e) {
            return AuthResponse.error("Login failed: " + e.getMessage());
        }
    }

    public User getUserByToken(String token) {
        // Basit token validation (gerçek uygulamada JWT kullanılmalı)
        try {
            // Token'dan user ID'yi extract et (örnek implementation)
            // Bu kısım gerçek JWT implementation ile değiştirilmeli
            return null; // Şimdilik null dönüyoruz
        } catch (Exception e) {
            return null;
        }
    }
}
