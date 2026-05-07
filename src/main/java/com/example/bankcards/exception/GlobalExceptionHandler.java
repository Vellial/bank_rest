package com.example.bankcards.exception;

import com.example.bankcards.dto.ValidationError;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.AccessDeniedException;
import java.util.List;

import com.example.bankcards.dto.ErrorResponse;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    // Валидация @Valid
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<ValidationError> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(e -> new ValidationError(e.getField(),
                        e.getDefaultMessage() != null ? e.getDefaultMessage() : "Ошибка валидации"))
                .toList();

        return ResponseEntity.badRequest()
                .body(new ErrorResponse("Ошибка валидации", errors));
    }

    // Карта не найдена
    @ExceptionHandler(CardNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCardNotFound(CardNotFoundException ex) {
        List<ValidationError> errors = List.of(new ValidationError("cardNumber", ex.getNumber()));
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getMessage(), errors));
    }
    @ExceptionHandler(CardByIdNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCardNotFound(CardByIdNotFoundException ex) {
        List<ValidationError> errors = List.of(new ValidationError("cardId", ex.getId().toString()));
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getMessage(), errors));
    }

    // Недостаточно средств
    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientFunds(InsufficientFundsException ex) {
        List<ValidationError> errors = List.of(new ValidationError("balance", "Недостаточно средств на карте"),
                new ValidationError("requestedAmount", String.valueOf(ex.getRequested())),
                new ValidationError("Available", String.valueOf(ex.getAvailable())));
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(ex.getMessage(), errors));
    }

    // Доступ запрещён
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse("Доступ запрещён", null));
    }

    // Общая ошибка
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Внутренняя ошибка сервера", null));
    }
}
