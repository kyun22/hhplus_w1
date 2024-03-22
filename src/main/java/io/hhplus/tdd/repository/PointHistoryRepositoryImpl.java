package io.hhplus.tdd.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.domain.PointHistory;
import io.hhplus.tdd.enums.TransactionType;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PointHistoryRepositoryImpl implements PointHistoryRepository{
	private final PointHistoryTable pointHistoryTable;

	public List<PointHistory> findAllByUserId(long id) {
		return pointHistoryTable.selectAllByUserId(id);
	}

	public PointHistory save(long userId, long amount, TransactionType type) {
		return pointHistoryTable.insert(userId, amount, type, System.currentTimeMillis());
	}
}
