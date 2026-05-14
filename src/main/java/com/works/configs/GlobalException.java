package com.works.configs;

import com.works.dto.ErrorResponseDto;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalException {

    // Servis katmanında fırlattığımız throw new RuntimeException(...) hatalarını yakalar
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponseDto> handleRuntimeException(@NonNull RuntimeException ex) {

        // Gereksinime uygun formattaki DTO'yu hazırlıyoruz (success: false)
        ErrorResponseDto errorDto = new ErrorResponseDto(false, ex.getMessage());

        // 400 Bad Request durum kodu ile fırlatıyoruz
        return new ResponseEntity<>(errorDto, HttpStatus.BAD_REQUEST);
    }

    // DTO'daki @NotBlank, @Email vb. Validasyon hatalarını yakalar
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidationExceptions(MethodArgumentNotValidException ex) {

        // Tüm alanlardaki validasyon hatalarını toplayıp tek bir mesaja dönüştürüyoruz
        StringBuilder errorMessage = new StringBuilder("Doğrulama Hatası: ");
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errorMessage.append(error.getField()).append(" (").append(error.getDefaultMessage()).append("), ");
        });

        ErrorResponseDto errorDto = new ErrorResponseDto(false, errorMessage.toString());

        return new ResponseEntity<>(errorDto, HttpStatus.BAD_REQUEST);
    }
}