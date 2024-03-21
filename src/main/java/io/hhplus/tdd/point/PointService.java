package io.hhplus.tdd.point;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import io.hhplus.tdd.database.LockByKey;
import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.dto.PointHistoryListResponse;
import io.hhplus.tdd.dto.UserPointRequest;
import io.hhplus.tdd.dto.UserPointResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PointService {

	private final UserPointTable userPointTable;
	private final PointHistoryTable pointHistoryTable;
	private final LockByKey lockByKey;

	public UserPointResponse charge(long id, UserPointRequest.Charge request) {
		UserPoint result;
		try {
			lockByKey.lock(id);
			UserPointResponse userPoint = getUserPoint(id);
			result = updateUserPoint(id, userPoint.getPoint() + request.getAmount());
			insertPointHistory(id, request.getAmount(), TransactionType.CHARGE);
		} finally {
			lockByKey.unlock(id);
		}

		return UserPointResponse.builder()
		  .id(result.id())
		  .point(result.point())
		  .updateMillis(result.updateMillis())
		  .build();
	}

	public UserPointResponse use(long id, UserPointRequest.Use request) {
		UserPoint result;
		try {
			lockByKey.lock(id);
			UserPointResponse userPoint = getUserPoint(id);

			if (userPoint.getPoint() < request.getAmount())
				throw new PointException(PointErrorResult.USER_POINT_IS_NOT_ENOUGH);

			result = updateUserPoint(id, userPoint.getPoint() - request.getAmount());
			insertPointHistory(id, request.getAmount(), TransactionType.USE);
		} finally {
			lockByKey.unlock(id);
		}

		return UserPointResponse.builder()
		  .id(result.id())
		  .point(result.point())
		  .updateMillis(result.updateMillis())
		  .build();
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
		if (histories.isEmpty())
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
