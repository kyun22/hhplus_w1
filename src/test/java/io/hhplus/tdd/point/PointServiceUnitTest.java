package io.hhplus.tdd.point;

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.hhplus.tdd.domain.PointHistory;
import io.hhplus.tdd.domain.UserPoint;
import io.hhplus.tdd.dto.ErrorResponse;
import io.hhplus.tdd.dto.PointHistoryListResponse;
import io.hhplus.tdd.dto.UserPointRequest;
import io.hhplus.tdd.dto.UserPointResponse;
import io.hhplus.tdd.enums.PointErrorResult;
import io.hhplus.tdd.enums.TransactionType;
import io.hhplus.tdd.exception.PointException;
import io.hhplus.tdd.repository.PointHistoryRepository;
import io.hhplus.tdd.repository.UserPointRepository;
import io.hhplus.tdd.repository.UserPointRepositoryImpl;
import io.hhplus.tdd.utils.LockByKey;

public class PointServiceUnitTest {

	private PointService pointService;

	@BeforeEach
	void setUp() {
		LockByKey lockByKey = new LockByKey();
		UserPointRepository userPointRepository = new UserPointRepositoryStub();
		PointHistoryRepository pointHistoryRepository = new PointHistoryRepositoryStub();
		pointService = new PointService(userPointRepository, pointHistoryRepository, lockByKey);
	}

	@DisplayName("유저포인트 충전 성공")
	@Test
	void testChargeSuccess() {
		UserPointResponse charged = pointService.charge(1L, new UserPointRequest.Charge(1000L));
		assertThat(charged.getPoint()).isEqualTo(1000L);
	}

	@DisplayName("유저포인트 충전 시 히스토리 저장")
	@Test
	void testChargeSaveHistory() {
		pointService.charge(2L, new UserPointRequest.Charge(1000L));
		pointService.charge(2L, new UserPointRequest.Charge(1000L));
		assertThat(pointService.getPointHistory(2L).size()).isEqualTo(2);
	}

	@DisplayName("유저포인트 사용 시 잔여 포인트가 부족한 경우 Exception 발생")
	@Test
	void testUsePointFail_WhenPointNotEnough() {
		assertThatThrownBy(() -> pointService.use(1L, new UserPointRequest.Use(1000L)))
		  .isInstanceOf(PointException.class)
		  .hasMessage(PointErrorResult.USER_POINT_IS_NOT_ENOUGH.getMessage())
		;
	}

	@DisplayName("유저포인트 사용 성공")
	@Test
	void testUseSuccess() {
		UserPointResponse use = pointService.use(3L, new UserPointRequest.Use(1000L));
		assertThat(use.getId()).isEqualTo(3L);
	}

	@DisplayName("유저포인트 사용 시 히스토리 저장")
	@Test
	void testUseSaveHistory() {
		pointService.use(3L, new UserPointRequest.Use(1000L));
		pointService.use(3L, new UserPointRequest.Use(1000L));
		assertThat(pointService.getPointHistory(3L).size()).isEqualTo(2);
	}

	@DisplayName("유저 포인트 조회 성공")
	@Test
	void testGetUserPointById() {
		UserPointResponse userPoint = pointService.getUserPoint(1L);
		assertThat(userPoint.getId()).isEqualTo(1L);
	}

	@DisplayName("유저 히스토리 조회 성공")
	@Test
	void testGetHistoriesById() {
		PointHistoryListResponse histories = pointService.getPointHistory(1L);
		assertThat(histories.size()).isEqualTo(2);
	}

	private class UserPointRepositoryStub implements UserPointRepository {
		@Override
		public UserPoint merge(long id, long point) {
			return new UserPoint(id, point, System.currentTimeMillis());
		}

		@Override
		public UserPoint findById(long id) {
			if (id == 3) {
				return new UserPoint(id, 2000, System.currentTimeMillis());
			}
			return new UserPoint(id, 0, System.currentTimeMillis());
		}
	}

	private class PointHistoryRepositoryStub implements PointHistoryRepository {
		@Override
		public List<PointHistory> findAllByUserId(long id) {
			List<PointHistory> list = new ArrayList<>();
			list.add(new PointHistory(0, 2, 1000, TransactionType.CHARGE, System.currentTimeMillis()));
			list.add(new PointHistory(0, 2, 1000, TransactionType.CHARGE, System.currentTimeMillis()));
			return list;
		}

		@Override
		public PointHistory save(long userId, long amount, TransactionType type) {
			return null;
		}
	}
}
