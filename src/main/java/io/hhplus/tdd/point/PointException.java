package io.hhplus.tdd.point;

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
