package com.packingmate.common;

import java.util.List;

public record ApiError(String code, String message, List<FieldViolation> violations) {
    public ApiError {
        violations = List.copyOf(violations);
    }

    public record FieldViolation(String field, String message) {
    }
}
