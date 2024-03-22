package io.hhplus.tdd.database;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.hhplus.tdd.domain.PointHistory;
import io.hhplus.tdd.enums.TransactionType;

class PointHistoryTableTest {

	private PointHistoryTable pointHistoryTable;

	@BeforeEach
	void setUp() {
		pointHistoryTable = new PointHistoryTable();
	}

	/**
	 * PointHistory 기능
	 * insert History (mode : charge or user)
	 * select by user id
	 */

	@DisplayName("존재하지 않는 유저 포인트 조회 - empty list 반환")
	@Test
	void selectByNonExistUserId(){

		List<PointHistory> pointHistories = pointHistoryTable.selectAllByUserId(1L);
		assertThat(pointHistories).isEmpty();
	}

	@DisplayName("PointHistory INSERT 성공")
	@Test
	void insertSuccess(){
		long updateMillis = System.currentTimeMillis();
		PointHistory insert = pointHistoryTable.insert(1L, 10000, TransactionType.CHARGE, updateMillis);
		assertThat(insert.updateMillis()).isEqualTo(updateMillis);
	}

	@DisplayName("같은 유저에게 히스토리 insert 하면 누적으로 저장")
	@Test
	void multipleInsertSuccess(){
		PointHistory insert = pointHistoryTable.insert(1L, 10000, TransactionType.CHARGE, System.currentTimeMillis());
		PointHistory insert2 = pointHistoryTable.insert(1L, 20000, TransactionType.CHARGE, System.currentTimeMillis());
		PointHistory insert3 = pointHistoryTable.insert(1L, 30000, TransactionType.CHARGE, System.currentTimeMillis());

		List<PointHistory> list = pointHistoryTable.selectAllByUserId(1L);
		assertThat(list.size()).isEqualTo(3);
	}

	@DisplayName("여러건의 insert 요청이 들어오는 경우 순차로 수행")
	@Test
	void testMethodName(){
		PointHistory insert = pointHistoryTable.insert(1L, 10000, TransactionType.CHARGE, System.currentTimeMillis());
		PointHistory insert2 = pointHistoryTable.insert(1L, 20000, TransactionType.CHARGE, System.currentTimeMillis());
		PointHistory insert3 = pointHistoryTable.insert(1L, 30000, TransactionType.CHARGE, System.currentTimeMillis());

		List<PointHistory> list = pointHistoryTable.selectAllByUserId(1L);
		assertThat(list.get(0).amount()).isEqualTo(10000);
		assertThat(list.get(1).amount()).isEqualTo(20000);
		assertThat(list.get(2).amount()).isEqualTo(30000);
	}
}