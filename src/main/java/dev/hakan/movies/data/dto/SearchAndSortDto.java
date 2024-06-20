package dev.hakan.movies.data.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import dev.hakan.movies.data.model.Movie;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NonNull
@AllArgsConstructor
@NoArgsConstructor
public class SearchAndSortDto {

    private Movie movie;
    private String selectedField;
    @JsonProperty("pageable")
    private PageableDto pageableDto;
}
