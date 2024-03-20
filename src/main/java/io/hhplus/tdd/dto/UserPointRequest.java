package io.hhplus.tdd.dto;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
public class UserPointRequest {

	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Charge {
		@Min(0)
		private Long amount;
	}

	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Use {
		@Min(0)
		private Long amount;
	}
}
