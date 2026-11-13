package com.packingmate.service;

/** 필드 검증 이외의 여행 조건 검증 실패. */
public class InvalidChecklistRequestException extends RuntimeException {
    public InvalidChecklistRequestException(String message) {
        super(message);
    }
}
