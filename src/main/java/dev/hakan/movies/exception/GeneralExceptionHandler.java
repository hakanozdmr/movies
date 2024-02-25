package dev.hakan.movies.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GeneralExceptionHandler extends ResponseEntityExceptionHandler {

//    @NonNull
//    @Override
//    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
//                                                                  @NonNull HttpHeaders headers,
//                                                                  @NonNull HttpStatusCode status,
//                                                                  @NonNull WebRequest request) {
//        Map<String, String> errors = new HashMap<>();
//        ex.getBindingResult().getAllErrors().forEach(error ->{
//            String fieldName = ((FieldError) error).getField();
//            String errorMessage = error.getDefaultMessage();
//            errors.put(fieldName, errorMessage);
//        });
//
//        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
//    }
//    @ExceptionHandler(GenericException.class)
//    public ResponseEntity<?> customerNotFoundExceptionHandler(GenericException exception)  {
//        Map<String,Object> errors  = new HashMap<>();
//        errors.put("message",exception.getErrorCode());
//        return ResponseEntity
//                .status(exception.getHttpStatus())
//                .body(errors);
//    }
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> runtimeExceptionHandler(RuntimeException exception)  {
        Map<String,Object> errors  = new HashMap<>();
        errors.put("message",exception.getMessage());
        return ResponseEntity
                .status(HttpStatus.LOCKED)
                .body(errors);
    }


//    @ExceptionHandler(GenericException.class)
//    public ResponseEntity<?> customerNotFoundExceptionHandler(GenericException exception)  {
//        Map<String,Object> errors  = new HashMap<>();
//        errors.put("message",exception.getErrorCode());
//        return ResponseEntity
//                .status(exception.getHttpStatus())
//                .body(errors);
//    }


}