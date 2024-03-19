package io.hhplus.tdd.point;

import org.springframework.stereotype.Service;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PointService {

	private final UserPointTable userPointTable;
	private final PointHistoryTable pointHistoryTable;

	public UserPoint charge(long id, long amount) {
		UserPoint userPoint = userPointTable.selectById(id);
		pointHistoryTable.insert(id, amount, TransactionType.CHARGE, System.currentTimeMillis());
		return userPointTable.insertOrUpdate(id, userPoint.point() + amount);
	}
}
