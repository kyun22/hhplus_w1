package io.hhplus.tdd;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.PointService;
import io.hhplus.tdd.point.UserPoint;

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
}
