package dev.hakan.movies.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class GenericException extends RuntimeException{
    private HttpStatus httpStatus;
    private ErrorCode errorCode;
    private String errorMessage;
}