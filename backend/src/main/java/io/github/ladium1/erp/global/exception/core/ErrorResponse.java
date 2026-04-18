package io.github.ladium1.erp.global.exception.core;

public record ErrorResponse(
        int status,
        String code,
        String message
) {
}