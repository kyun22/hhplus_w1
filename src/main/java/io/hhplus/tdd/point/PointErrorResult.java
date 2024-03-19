package io.hhplus.tdd.point;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PointErrorResult {
	USER_POINT_IS_NOT_ENOUGH(HttpStatus.BAD_REQUEST, "User point is not enough."),
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "User does not exist."),
	;

	private final HttpStatus status;
	private final String message;

}
