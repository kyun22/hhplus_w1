package io.hhplus.tdd.repository;

import org.springframework.stereotype.Repository;

import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.domain.UserPoint;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserPointRepository {

	private final UserPointTable userPointTable;

	public UserPoint merge(long id, long point) {
		return userPointTable.insertOrUpdate(id, point);
	}

	public UserPoint findById(long id) {
		return userPointTable.selectById(id);
	}
}
