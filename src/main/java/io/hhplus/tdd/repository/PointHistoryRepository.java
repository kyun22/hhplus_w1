package io.hhplus.tdd.repository;

import java.util.List;

import io.hhplus.tdd.domain.PointHistory;
import io.hhplus.tdd.enums.TransactionType;

public interface PointHistoryRepository {
	List<PointHistory> findAllByUserId(long id);

	PointHistory save(long userId, long amount, TransactionType type);
}
