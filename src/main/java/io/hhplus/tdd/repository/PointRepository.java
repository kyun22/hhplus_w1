package io.hhplus.tdd.repository;

import org.springframework.stereotype.Repository;

import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.domain.UserPoint;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PointRepository {

	private final UserPointTable userPointTable;

	public UserPoint merge(UserPoint userPoint) {
		return userPointTable.insertOrUpdate(userPoint.id(), userPoint.point());
	}

	public UserPoint findById(long id) {
		return userPointTable.selectById(id);
	}
}
