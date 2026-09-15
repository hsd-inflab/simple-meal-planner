package hsd.inflab.smp.controller;

import hsd.inflab.smp.service.RecipeNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(RecipeNotFoundException.class)
    public ResponseEntity<Void> handleRecipeNotFound() {
        return ResponseEntity.notFound().build();
    }
}
