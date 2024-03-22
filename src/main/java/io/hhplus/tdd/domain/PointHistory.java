package io.hhplus.tdd.domain;

import io.hhplus.tdd.enums.TransactionType;

public record PointHistory(
        long id,
        long userId,
        long amount,
        TransactionType type,
        long updateMillis
) {
}
