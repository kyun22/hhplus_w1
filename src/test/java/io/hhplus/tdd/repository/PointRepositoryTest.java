package io.hhplus.tdd.repository;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.domain.UserPoint;

class PointRepositoryTest {

	private PointRepository pointRepository;

	private UserPointTable userPointTable;

	@BeforeEach
	void setUp() {
		userPointTable = new UserPointTable();
		pointRepository = new PointRepository(userPointTable);
	}

	@DisplayName("유저 포인트 저장 성공")
	@Test
	void testSaveUserPoint(){
		UserPoint userPoint = pointRepository.merge(new UserPoint(1L, 1000L, System.currentTimeMillis()));
		assertThat(userPoint.id()).isEqualTo(1L);
		assertThat(userPoint.point()).isEqualTo(1000L);
	}

	@DisplayName("유저 포인트 조회 성공. 미저장 유저 조회는 포인트 0")
	@Test
	void testSelectUserPoint(){
		userPointTable.insertOrUpdate(1L, 1000L);
		UserPoint userPoint = pointRepository.findById(1L);
		UserPoint userPoint2 = pointRepository.findById(2L);
		assertThat(userPoint.point()).isEqualTo(1000L);
		assertThat(userPoint2.point()).isEqualTo(0L);
	}

}