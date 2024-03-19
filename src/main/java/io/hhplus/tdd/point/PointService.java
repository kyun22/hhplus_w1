package io.hhplus.tdd.point;

import java.util.List;

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
		UserPoint userPoint = getUserPoint(id);
		insertPointHistory(id, amount, TransactionType.CHARGE);
		return updateUserPoint(id, userPoint.point() + amount);
	}

	public UserPoint use(long id, long amount) {
		UserPoint userPoint = getUserPoint(id);

		if (userPoint.point() < amount)
			throw new PointException(PointErrorResult.USER_POINT_IS_NOT_ENOUGH);

		insertPointHistory(id, amount, TransactionType.USE);
		return updateUserPoint(id, userPoint.point() - amount);
	}

	private UserPoint updateUserPoint(long id, long amount1) {
		return userPointTable.insertOrUpdate(id, amount1);
	}

	private PointHistory insertPointHistory(long id, long amount, TransactionType use) {
		return pointHistoryTable.insert(id, amount, use, System.currentTimeMillis());
	}

	public UserPoint getUserPoint(long id) {
		return userPointTable.selectById(id);
	}

	public List<PointHistory> getPointHistory(long id) {

		List<PointHistory> histories = pointHistoryTable.selectAllByUserId(id);
		if(histories.isEmpty())
			throw new PointException(PointErrorResult.USER_NOT_FOUND);

		return histories;
	}
}
