package io.hhplus.tdd.dto;

public record ErrorResponse(
        String code,
        String message
) {
}
