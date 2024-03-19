package io.hhplus.tdd.point;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PointErrorResult {
	USER_POINT_IS_NOT_ENOUGH("User point is not enough."),
	;

	private final String message;

}
