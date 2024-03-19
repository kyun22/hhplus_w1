package io.hhplus.tdd.point;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.dto.PointHistoryListResponse;
import io.hhplus.tdd.dto.UserPointResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PointService {

	private final UserPointTable userPointTable;
	private final PointHistoryTable pointHistoryTable;

	public UserPoint charge(long id, long amount) {
		UserPointResponse userPoint = getUserPoint(id);
		insertPointHistory(id, amount, TransactionType.CHARGE);
		return updateUserPoint(id, userPoint.getPoint() + amount);
	}

	public UserPoint use(long id, long amount) {
		UserPointResponse userPoint = getUserPoint(id);

		if (userPoint.getPoint() < amount)
			throw new PointException(PointErrorResult.USER_POINT_IS_NOT_ENOUGH);

		insertPointHistory(id, amount, TransactionType.USE);
		return updateUserPoint(id, userPoint.getPoint() - amount);
	}

	private UserPoint updateUserPoint(long id, long amount1) {
		return userPointTable.insertOrUpdate(id, amount1);
	}

	private PointHistory insertPointHistory(long id, long amount, TransactionType use) {
		return pointHistoryTable.insert(id, amount, use, System.currentTimeMillis());
	}

	public UserPointResponse getUserPoint(long id) {
		UserPoint userPoint = userPointTable.selectById(id);

		return UserPointResponse.builder()
		  .id(userPoint.id())
		  .point(userPoint.point())
		  .updateMillis(userPoint.updateMillis())
		  .build();
	}

	public PointHistoryListResponse getPointHistory(long id) {

		List<PointHistory> histories = pointHistoryTable.selectAllByUserId(id);
		if(histories.isEmpty())
			throw new PointException(PointErrorResult.USER_NOT_FOUND);

		List<PointHistoryListResponse.PointHistoryResponse> list = histories.stream()
		  .map(pointHistory -> PointHistoryListResponse.PointHistoryResponse.builder()
			.id(pointHistory.id())
			.userId(pointHistory.userId())
			.amount(pointHistory.amount())
			.type(pointHistory.type())
			.updateMillis(pointHistory.updateMillis())
			.build())
		  .collect(Collectors.toList());

		return new PointHistoryListResponse(list);
	}
}
