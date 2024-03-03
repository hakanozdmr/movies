package dev.hakan.movies.data.dto;

import dev.hakan.movies.data.enums.SortingDirection;

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
    private String sortedField;
    private SortingDirection direction;
    private String selectedField;
}
