package io.hhplus.tdd.repository;

import io.hhplus.tdd.domain.UserPoint;

public interface UserPointRepository {
	UserPoint merge(long id, long point);

	UserPoint findById(long id);
}
