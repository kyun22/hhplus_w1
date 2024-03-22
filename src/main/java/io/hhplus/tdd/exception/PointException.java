package io.hhplus.tdd.exception;

import io.hhplus.tdd.enums.PointErrorResult;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PointException extends RuntimeException{
	private final PointErrorResult errorResult;

	@Override
	public String getMessage() {
		return errorResult.getMessage();
	}
}
