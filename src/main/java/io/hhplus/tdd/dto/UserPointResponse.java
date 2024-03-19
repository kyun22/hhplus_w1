package io.hhplus.tdd.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserPointResponse {
	private Long id;
	private Long point;
	private Long updateMillis;
}
