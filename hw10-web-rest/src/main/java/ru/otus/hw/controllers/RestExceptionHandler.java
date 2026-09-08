package ru.otus.hw.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.otus.hw.dto.ApiErrorResponse;
import ru.otus.hw.exceptions.EntityNotFoundException;

import java.util.LinkedHashMap;
import java.util.stream.Collectors;

@RestControllerAdvice/*(basePackageClasses = {
        AuthorController.class,
        GenreController.class,
        BookController.class,
        CommentController.class
})*/
public class RestExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiErrorResponse handleEntityNotFound(EntityNotFoundException ex, HttpServletRequest request) {
        return ApiErrorResponse.of(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiErrorResponse handleIllegalState(IllegalStateException ex, HttpServletRequest request) {
        return ApiErrorResponse.of(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler({IllegalArgumentException.class, HttpMessageNotReadableException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleBadRequest(Exception ex, HttpServletRequest request) {
        return ApiErrorResponse.of(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        var validationErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        RestExceptionHandler::fieldName,
                        RestExceptionHandler::fieldMessage,
                        (first, second) -> first,
                        LinkedHashMap::new
                ));
        return ApiErrorResponse.of(HttpStatus.BAD_REQUEST, "Validation failed", request.getRequestURI(),
                validationErrors);
    }

    private static String fieldName(FieldError fieldError) {
        return fieldError.getField();
    }

    private static String fieldMessage(FieldError fieldError) {
        return fieldError.getDefaultMessage();
    }
}
