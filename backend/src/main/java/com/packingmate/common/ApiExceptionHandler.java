package com.packingmate.common;

import com.packingmate.service.InvalidChecklistRequestException;
import java.util.Comparator;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidation(MethodArgumentNotValidException exception) {
        List<ApiError.FieldViolation> violations = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> new ApiError.FieldViolation(error.getField(), error.getDefaultMessage()))
                .sorted(Comparator.comparing(ApiError.FieldViolation::field))
                .toList();
        return new ApiError("VALIDATION_ERROR", "요청 값이 올바르지 않습니다.", violations);
    }

    @ExceptionHandler(InvalidChecklistRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleInvalidRequest(InvalidChecklistRequestException exception) {
        return new ApiError("INVALID_TRIP", exception.getMessage(), List.of());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleUnreadableBody(HttpMessageNotReadableException exception) {
        return new ApiError("INVALID_REQUEST_BODY", "요청 본문 또는 날짜 형식이 올바르지 않습니다.", List.of());
    }
}
