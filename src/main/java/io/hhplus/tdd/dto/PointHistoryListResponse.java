package io.hhplus.tdd.dto;

import java.util.ArrayList;
import java.util.List;

import io.hhplus.tdd.enums.TransactionType;
import lombok.Builder;
import lombok.Getter;

@Getter
public class PointHistoryListResponse {
	private List<PointHistoryResponse> histories = new ArrayList<>();

	@Getter
	@Builder
	public static class PointHistoryResponse {
		private Long id;
		private Long userId;
		private Long amount;
		private TransactionType type;
		private Long updateMillis;
	}

	public PointHistoryListResponse(List<PointHistoryResponse> histories) {
		this.histories = histories;
	}

	public int size() {
		return this.histories.size();
	}
}
