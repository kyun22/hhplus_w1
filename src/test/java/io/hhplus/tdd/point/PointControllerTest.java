package io.hhplus.tdd.point;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import io.hhplus.tdd.ApiControllerAdvice;
import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;

class PointControllerTest {
	MockMvc mockMvc;
	private UserPointTable userPointTable;
	private PointHistoryTable pointHistoryTable;

	@BeforeEach
	void setUp() {
		userPointTable = new UserPointTable();
		pointHistoryTable = new PointHistoryTable();
		PointService pointService = new PointService(userPointTable, pointHistoryTable);
		PointController pointController = new PointController(pointService);
		ApiControllerAdvice apiControllerAdvice = new ApiControllerAdvice();
		mockMvc = MockMvcBuilders.standaloneSetup(pointController)
		  .setControllerAdvice(apiControllerAdvice).build();
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
		ResultActions resultActions = mockMvc.perform(
		  get("/point/1"));

		resultActions.andExpect(status().isOk())
		  .andExpect(jsonPath("id").value(1))
		  .andExpect(jsonPath("point").value(0));
	}

	@DisplayName("유저의 포인트 조회 성공")
	@Test
	void selectExistUserPoint() throws Exception {
		userPointTable.insertOrUpdate(1L, 1000);

		ResultActions resultActions = mockMvc.perform(
		  get("/point/1"));

		resultActions.andExpect(status().isOk())
		  .andExpect(jsonPath("id").value(1))
		  .andExpect(jsonPath("point").value(1000));
	}

	@DisplayName("히스토리 조회 실패 - 존재하지 않는 유저")
	@Test
	void selectHistoryFailUserNotFound() throws Exception {
		ResultActions resultActions = mockMvc.perform(get("/point/1/histories"));
		resultActions.andExpect(status().isNotFound());
	}

	@DisplayName("히스토리 조회 성공")
	@Test
	void testMethodName() throws Exception {
		pointHistoryTable.insert(1L, 10000, TransactionType.CHARGE, System.currentTimeMillis());
		pointHistoryTable.insert(1L, 20000, TransactionType.USE, System.currentTimeMillis());
		pointHistoryTable.insert(1L, 30000, TransactionType.CHARGE, System.currentTimeMillis());

		ResultActions resultActions = mockMvc.perform(get("/point/1/histories"));
		resultActions.andExpect(status().isOk())
		  .andExpect(jsonPath("$.length()").value(3))
		  .andExpect(jsonPath("$[0].amount").value(10000))
		  .andExpect(jsonPath("$[1].type").value(TransactionType.USE.name()));
	}

}