package dev.hakan.movies.data.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String token;
    private String userId;
    private String email;
    private String name;
    private boolean success;
    private String message;
    
    public AuthResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    public static AuthResponse success(String token, String userId, String email, String name) {
        return new AuthResponse(token, userId, email, name, true, "Authentication successful");
    }
    
    public static AuthResponse error(String message) {
        return new AuthResponse(false, message);
    }
}

