package io.hhplus.tdd.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.domain.PointHistory;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PointHistoryRepository {
	private final PointHistoryTable pointHistoryTable;

	public List<PointHistory> findAllByUserId(long id) {
		return pointHistoryTable.selectAllByUserId(id);
	}

	public PointHistory save(PointHistory pointHistory) {
		return pointHistoryTable.insert(pointHistory.userId(), pointHistory.amount(), pointHistory.type(),
		  pointHistory.updateMillis());
	}
}
