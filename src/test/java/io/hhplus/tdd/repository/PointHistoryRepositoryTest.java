package io.hhplus.tdd.repository;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.domain.PointHistory;
import io.hhplus.tdd.enums.TransactionType;

public class PointHistoryRepositoryTest {

	private PointHistoryRepository pointHistoryRepository;

	@BeforeEach
	void setUp() {
		PointHistoryTable pointHistoryTable = new PointHistoryTable();
		pointHistoryRepository = new PointHistoryRepositoryImpl(pointHistoryTable);
	}

	@DisplayName("포인트히스토리 insert 성공")
	@Test
	void testInsert() {
		PointHistory pointHistory = saveOneHistory();
		assertThat(pointHistory.amount()).isEqualTo(1000);
		assertThat(pointHistory.type()).isEqualTo(TransactionType.CHARGE);
	}

	@DisplayName("포인트히스토리 조회 시, 히스토리가 존재하지 않으면 빈 리스트(empty list) 반환")
	@Test
	void testFindAllByUserIdNonExists() {
		List<PointHistory> histories = pointHistoryRepository.findAllByUserId(1L);
		assertThat(histories).isEmpty();
	}

	@DisplayName("포인트히스토리 조회 시, 히스토리가 존재하면 모든 리스트 반환 ")
	@Test
	void testFindAllByUserIdSuccess() {
		for (int i = 0; i < 10; i++) {
			saveOneHistory();
		}
		List<PointHistory> histories = pointHistoryRepository.findAllByUserId(1L);
		assertThat(histories.size()).isEqualTo(10);
	}

	private PointHistory saveOneHistory() {
		return pointHistoryRepository.save(1L, 1000, TransactionType.CHARGE);
	}

}
