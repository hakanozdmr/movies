package dev.hakan.movies.data.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WatchlistRequest {
    @NotBlank(message = "Movie ID is required")
    private String movieId;
}

