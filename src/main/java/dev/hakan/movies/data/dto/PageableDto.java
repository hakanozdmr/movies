package dev.hakan.movies.data.dto;

import dev.hakan.movies.data.enums.SortingDirection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageableDto {

    private int pageNumber;
    private int pageSize;
    private String sortedField;
    private SortingDirection direction;
}
