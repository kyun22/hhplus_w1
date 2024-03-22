package io.hhplus.tdd.point;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.domain.PointHistory;
import io.hhplus.tdd.domain.UserPoint;
import io.hhplus.tdd.dto.PointHistoryListResponse;
import io.hhplus.tdd.dto.UserPointRequest;
import io.hhplus.tdd.dto.UserPointResponse;
import io.hhplus.tdd.enums.PointErrorResult;
import io.hhplus.tdd.exception.PointException;
import io.hhplus.tdd.repository.PointHistoryRepository;
import io.hhplus.tdd.repository.PointHistoryRepositoryImpl;
import io.hhplus.tdd.repository.UserPointRepository;
import io.hhplus.tdd.repository.UserPointRepositoryImpl;
import io.hhplus.tdd.utils.LockByKey;

public class PointServiceTest {

	private PointService pointService;
	private UserPointTable userPointTable;
	private PointHistoryTable pointHistoryTable;
	private LockByKey lockByKey;

	@BeforeEach
	void setUp() {
		lockByKey = new LockByKey();
		userPointTable = new UserPointTable();
		UserPointRepository userPointRepository = new UserPointRepositoryImpl(userPointTable);
		pointHistoryTable = new PointHistoryTable();
		PointHistoryRepository pointHistoryRepository = new PointHistoryRepositoryImpl(pointHistoryTable);
		pointService = new PointService(userPointRepository, pointHistoryRepository, lockByKey);
	}

	/**
	 * UserPoint 서비스
	 *   - UserPoint use, charge 기능
	 *   - use, charge 시 History 추가
	 *   - select 기능도 제공?
	 */

	@DisplayName("유저 포인트 충전 성공")
	@Test
	void chargeUserPointSuccess() {
		chargePoint(1L, 1000L);
		chargePoint(1L, 1000L);

		// then
		UserPoint userPoint = userPointTable.selectById(1L);
		assertThat(userPoint.point()).isEqualTo(2000L);
	}

	@DisplayName("유저 포인트 충전 시 히스토리도 저장 됨.")
	@Test
	void insertHistoryWhenChargeUserPoint() {

		chargePoint(1L, 1000L);
		chargePoint(1L, 1000L);

		// then
		List<PointHistory> pointHistories = pointHistoryTable.selectAllByUserId(1L);
		assertThat(pointHistories.size()).isEqualTo(2);
	}

	@DisplayName("유저 포인트 사용 실패 - 포인트 잔고 부족 ")
	@Test
	void useUserPointFail() {
		PointException exception = assertThrows(PointException.class, () -> usePoint(1L, 1000));
		assertThat(exception.getErrorResult()).isEqualTo(PointErrorResult.USER_POINT_IS_NOT_ENOUGH);
	}

	@DisplayName("유저 포인트 사용 성공")
	@Test
	void useUserPointSuccess() {
		chargePoint(1L, 1000L);
		usePoint(1L, 100L);
		UserPointResponse used = usePoint(1L, 100L);

		assertThat(used.getPoint()).isEqualTo(800L);
	}

	@DisplayName("유저 포인트 충전 시 히스토리도 저장 됨.")
	@Test
	void insertHistoryWhenUseUserPoint() {
		userPointTable.insertOrUpdate(1L, 10000L);
		usePoint(1L, 1000L);
		usePoint(1L, 1000L);

		// then
		List<PointHistory> pointHistories = pointHistoryTable.selectAllByUserId(1L);
		assertThat(pointHistories.size()).isEqualTo(2);
	}

	@DisplayName("포인트 조회 성공")
	@Test
	void selectUserPointSuccess() {
		UserPointResponse response = pointService.getUserPoint(1L);

		assertThat(response.getId()).isEqualTo(1L);
		assertThat(response.getPoint()).isEqualTo(0);
	}

	@DisplayName("포인트 히스토리 조회 성공")
	@Test
	void selectPointHistorySuccess() {
		chargePoint(1L, 1000);
		chargePoint(1L, 1000);
		PointHistoryListResponse history = pointService.getPointHistory(1L);
		assertThat(history.size()).isEqualTo(2);
	}

	private UserPointResponse chargePoint(long id, long amount) {
		return pointService.charge(id, new UserPointRequest.Charge(amount));
	}

	private UserPointResponse usePoint(long id, long amount) {
		return pointService.use(id, new UserPointRequest.Use(amount));
	}
}
