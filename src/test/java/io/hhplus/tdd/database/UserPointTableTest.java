package io.hhplus.tdd.database;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.hhplus.tdd.point.UserPoint;

class UserPointTableTest {

	private UserPointTable userPointTable;

	@BeforeEach
	void setUp() {
		userPointTable = new UserPointTable();
	}

	/**
	 * UserPointTable DB 기능을 테스트한다.
	 * 1. 조회
	 *   - 저장되지 않은 경우 : 해당 id를 갖는 포인트 0 반환
	 *   - 저장되어 있는 경우 : 저장된 UserPoint 반환
	 * 2. 저장
	 *   - 존재하지 않으면 insert
	 *   - 존재하면 해당 id에 amount 만큼 포인트 update
	 */

	@DisplayName("저장되지 않은 유저 포인트 조회 성공")
	@Test
	void selectByNonExistIdTest(){
		UserPoint userPoint = userPointTable.selectById(1L);

		assertThat(userPoint.id()).isEqualTo(1L);
		assertThat(userPoint.point()).isEqualTo(0);
	}

	@DisplayName("insert기능, update 성공")
	@Test
	void insertOrUpdateTest(){
		userPointTable.insertOrUpdate(0L, 1000);
		UserPoint userPoint = userPointTable.selectById(0L);
		assertThat(userPoint.point()).isEqualTo(1000L);

		userPointTable.insertOrUpdate(0L, 2000);
		userPoint = userPointTable.selectById(0L);
		assertThat(userPoint.point()).isEqualTo(2000L);
	}

	@DisplayName("저장된 유저 포인트 조회 성공")
	@Test
	void selectByExistIdTest(){
		userPointTable.insertOrUpdate(0L, 1000);
		UserPoint userPoint = userPointTable.selectById(0L);

		assertThat(userPoint.id()).isEqualTo(0L);
		assertThat(userPoint.point()).isEqualTo(1000L);
	}

}