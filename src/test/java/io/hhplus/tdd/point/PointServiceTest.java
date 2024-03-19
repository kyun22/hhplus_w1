package io.hhplus.tdd.point;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;

public class PointServiceTest {

	private PointService pointService;
	private UserPointTable userPointTable;
	private PointHistoryTable pointHistoryTable;

	@BeforeEach
	void setUp() {
		userPointTable = new UserPointTable();
		pointHistoryTable = new PointHistoryTable();
		pointService = new PointService(userPointTable, pointHistoryTable);
	}

	/**
	 * UserPoint 서비스
	 *   - UserPoint use, charge 기능
	 *   - use, charge 시 History 추가
	 *   - select 기능도 제공?
	 */

	@DisplayName("유저 포인트 충전 성공")
	@Test
	void chargeUserPointSuccess(){
		pointService.charge(1L, 1000L);
		pointService.charge(1L, 1000L);

		// then
		UserPoint userPoint = userPointTable.selectById(1L);
		assertThat(userPoint.point()).isEqualTo(2000L);
	}

	@DisplayName("유저 포인트 충전 시 히스토리도 저장 됨.")
	@Test
	void insertHistoryWhenChargeUserPoint(){

		pointService.charge(1L, 1000L);
		pointService.charge(1L, 1000L);

		// then
		List<PointHistory> pointHistories = pointHistoryTable.selectAllByUserId(1L);
		assertThat(pointHistories.size()).isEqualTo(2);
	}

	@DisplayName("유저 포인트 사용 실패 - 포인트 잔고 부족 ")
	@Test
	void useUserPointFail(){
		PointException exception = assertThrows(PointException.class, () -> pointService.use(1L, 1000));
		assertThat(exception.getErrorResult()).isEqualTo(PointErrorResult.USER_POINT_IS_NOT_ENOUGH);
	}

	@DisplayName("유저 포인트 사용 성공")
	@Test
	void useUserPointSuccess(){
		pointService.charge(1L, 1000L);
		pointService.use(1L, 100L);
		UserPoint used = pointService.use(1L, 100L);

		assertThat(used.point()).isEqualTo(800L);
	}

	@DisplayName("유저 포인트 충전 시 히스토리도 저장 됨.")
	@Test
	void insertHistoryWhenUseUserPoint(){
		userPointTable.insertOrUpdate(1L, 10000L);
		pointService.use(1L, 1000L);
		pointService.use(1L, 1000L);

		// then
		List<PointHistory> pointHistories = pointHistoryTable.selectAllByUserId(1L);
		assertThat(pointHistories.size()).isEqualTo(2);
	}

	@DisplayName("포인트 조회 성공")
	@Test
	void selectUserPointSuccess(){
		UserPoint userPoint = pointService.getUserPoint(1L);

		assertThat(userPoint.id()).isEqualTo(1L);
		assertThat(userPoint.point()).isEqualTo(0);
	}

	@DisplayName("포인트 히스토리 조회 성공")
	@Test
	void selectPointHistorySuccess(){
		pointService.charge(1L, 1000);
		pointService.charge(1L, 1000);
		List<PointHistory> history = pointService.getPointHistory(1L);
		assertThat(history.size()).isEqualTo(2);
	}



}
