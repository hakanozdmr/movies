package dev.hakan.movies.data.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GoogleAuthRequest {
    
    @NotBlank(message = "Google ID token is required")
    private String idToken;
}
