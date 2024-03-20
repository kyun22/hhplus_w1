package io.hhplus.tdd.point;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.hhplus.tdd.ApiControllerAdvice;
import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.dto.UserPointRequest;

class PointControllerTest {
	MockMvc mockMvc;
	private UserPointTable userPointTable;
	private PointHistoryTable pointHistoryTable;
	private ObjectMapper objectMapper;

	@BeforeEach
	void setUp() {
		userPointTable = new UserPointTable();
		pointHistoryTable = new PointHistoryTable();
		PointService pointService = new PointService(userPointTable, pointHistoryTable);
		PointController pointController = new PointController(pointService);
		ApiControllerAdvice apiControllerAdvice = new ApiControllerAdvice();
		mockMvc = MockMvcBuilders.standaloneSetup(pointController)
		  .setControllerAdvice(apiControllerAdvice).build();
		objectMapper = new ObjectMapper();
	}

	@DisplayName("/point로 접근하는 경우 404")
	@Test
	void pageNotFound() throws Exception {
		mockMvc.perform(get("/point"))
		  .andExpect(status().isNotFound());
	}

	@DisplayName("존재하지 않는 유저의 포인트 조회 성공 - point가 0")
	@Test
	void selectNonExistUserPoint() throws Exception {

		mockMvc.perform(
			get("/point/1"))
		  .andExpect(status().isOk())
		  .andExpect(jsonPath("id").value(1))
		  .andExpect(jsonPath("point").value(0));
	}

	@DisplayName("유저의 포인트 조회 성공")
	@Test
	void selectExistUserPoint() throws Exception {
		insertUserPoint(1L, 1000);

		mockMvc.perform(
			get("/point/1"))
		  .andExpect(status().isOk())
		  .andExpect(jsonPath("id").value(1))
		  .andExpect(jsonPath("point").value(1000));
	}

	@DisplayName("히스토리 조회 실패 - 존재하지 않는 유저")
	@Test
	void selectHistoryFailUserNotFound() throws Exception {
		mockMvc.perform(get("/point/1/histories"))
		  .andExpect(status().isNotFound());
	}

	@DisplayName("히스토리 조회 성공")
	@Test
	void selectHistorySuccess() throws Exception {
		insertPointHistory(1L, 1000, TransactionType.CHARGE);
		insertPointHistory(1L, 2000, TransactionType.USE);
		insertPointHistory(1L, 3000, TransactionType.CHARGE);

		mockMvc.perform(get("/point/1/histories"))
		  .andExpect(status().isOk())
		  .andExpect(jsonPath("histories.length()").value(3))
		  .andExpect(jsonPath("histories[0].amount").value(1000))
		  .andExpect(jsonPath("histories[1].type").value(TransactionType.USE.name()));
	}

	@DisplayName("포인트 충전 실패 - amount가 음수")
	@Test
	void chargeFailWhenAmountLessThanZero() throws Exception {
		mockMvc.perform(patch("/point/1/charge")
			.contentType(MediaType.APPLICATION_JSON)
			.content(makeUserPointRequest(-1000L, TransactionType.CHARGE)))
		  // .andDo(print());
		  .andExpect(status().isBadRequest());
	}

	@DisplayName("포인트 충전 성공")
	@Test
	void chargePointTest() throws Exception {
		mockMvc.perform(patch("/point/1/charge")
			.contentType(MediaType.APPLICATION_JSON)
			.content(makeUserPointRequest(1000L, TransactionType.CHARGE)))
		  // .andDo(print());
		  .andExpect(status().isOk())
		  .andExpect(jsonPath("id").value(1))
		  .andExpect(jsonPath("point").value(1000));
	}

	@DisplayName("포인트 사용 실패 - 잔액이 부족함")
	@Test
	void usePointFailNotEnoughPoint() throws Exception {
		mockMvc.perform(patch("/point/1/use")
		  .contentType(MediaType.APPLICATION_JSON)
		  .content(makeUserPointRequest(100L, TransactionType.USE))
		).andExpect(status().isBadRequest())
		;
	}

	@DisplayName("포인트 사용 실패 - amount가 음수")
	@Test
	void usePointFailWhenAmountLessThanZero() throws Exception {
		mockMvc.perform(patch("/point/1/use")
		  .contentType(MediaType.APPLICATION_JSON)
		  .content(makeUserPointRequest(-100L, TransactionType.USE))
		).andExpect(status().isBadRequest())
		;
	}

	@DisplayName("포인트 사용 성공")
	@Test
	void usePointSuccess() throws Exception {
		insertUserPoint(1L, 1000L);

		mockMvc.perform(patch("/point/1/use")
			.contentType(MediaType.APPLICATION_JSON)
			.content(makeUserPointRequest(100L, TransactionType.USE))
		  ).andExpect(status().isOk())
		  .andExpect(jsonPath("id").value(1))
		  .andExpect(jsonPath("point").value(900));
	}

	@DisplayName("유저 포인트 충전, 사용시 히스로리 저장 성공")
	@Test
	void usePointSaveHistorySuccess() throws Exception {
		mockMvc.perform(patch("/point/1/charge")
		  .contentType(MediaType.APPLICATION_JSON)
		  .content(makeUserPointRequest(1000L, TransactionType.CHARGE)))
		  .andExpect(status().isOk());

		mockMvc.perform(patch("/point/1/use")
		  .contentType(MediaType.APPLICATION_JSON)
		  .content(makeUserPointRequest(100L, TransactionType.USE)))
		  .andExpect(status().isOk());

		mockMvc.perform(get("/point/1/histories"))
		  .andExpect(jsonPath("histories.length()").value(2))
		  .andExpect(jsonPath("histories[0].type").value(TransactionType.CHARGE.name()))
		  .andExpect(jsonPath("histories[1].type").value(TransactionType.USE.name()));
	}


	private UserPoint insertUserPoint(long id, long amount) {
		return userPointTable.insertOrUpdate(id, amount);
	}

	private PointHistory insertPointHistory(long userId, int amount, TransactionType type) {
		return pointHistoryTable.insert(userId, amount, type, System.currentTimeMillis());
	}

	private String makeUserPointRequest(long amount, TransactionType type) throws JsonProcessingException {
		return objectMapper.writeValueAsString(
		  type.equals(TransactionType.CHARGE)
			? new UserPointRequest.Charge(amount)
			: new UserPointRequest.Use(amount)
		);
	}

}
