package io.hhplus.tdd.point;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.hhplus.tdd.ApiControllerAdvice;
import io.hhplus.tdd.database.LockByKey;
import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.dto.UserPointRequest;

public class UserPointMultiThreadTest {
	private static final Logger log = LoggerFactory.getLogger(PointController.class);
	private final static int N_THREADS = 10;
	private final static int MAX_EXECUTE_COUNT = 10;

	private UserPointTable userPointTable;
	private ExecutorService executorService;
	private CountDownLatch latch;
	private LockByKey lockByKey;
	private PointHistoryTable pointHistoryTable;
	private PointService pointService;
	private MockMvc mockMvc;
	private ObjectMapper objectMapper;

	@BeforeEach
	void setUp() {
		userPointTable = new UserPointTable();
		lockByKey = new LockByKey();
		pointHistoryTable = new PointHistoryTable();
		pointService = new PointService(userPointTable, pointHistoryTable, lockByKey);
		executorService = Executors.newFixedThreadPool(N_THREADS);
		latch = new CountDownLatch(MAX_EXECUTE_COUNT);
		PointController pointController = new PointController(pointService);
		ApiControllerAdvice apiControllerAdvice = new ApiControllerAdvice();
		mockMvc = MockMvcBuilders.standaloneSetup(pointController).setControllerAdvice(apiControllerAdvice).build();
		objectMapper = new ObjectMapper();
	}

	/**
	 * 동시에 service 호출이 여러개 들어오는 경우, lock이 해제되길 기다리고, 순차로 실행 테스트
	 *   - charge(1L, 10L) 반복 실행
	 *   - 예상 결과 : 10L * 반복횟수(20) = 200
	 *   - thread-safe 하지 않은 경우 제대로 포인트가 누적되지 않을 것 (접근당시 공유자원 + 10)
	 */
	@DisplayName("PointService 멀티 스레드 테스트 - 충전 반복")
	@Test
	void testMultiInsertOrUpdate() throws InterruptedException {

		for (int i = 0; i < MAX_EXECUTE_COUNT; i++) {
			executorService.submit(() -> {
				pointService.charge(1L, new UserPointRequest.Charge(10L));
				latch.countDown();
			});
		}

		latch.await();
		assertThat(getPoint(1L)).isEqualTo(10 * MAX_EXECUTE_COUNT);
		assertThat(getHistoriesSize()).isEqualTo(MAX_EXECUTE_COUNT);
	}

	private int getHistoriesSize() {
		return pointHistoryTable.selectAllByUserId(1L).size();
	}

	private long getPoint(long id) {
		return userPointTable.selectById(id).point();
	}

	private void transactionChargeAndManyUse(long id, long charge, long... use) {
		try {
			lockByKey.lock(-1L);
			pointService.charge(id, new UserPointRequest.Charge(charge));
			log.info("point : {}", getPoint(id));
			for (long u : use) {
				pointService.use(id, new UserPointRequest.Use(u));
				log.info("point : {}", getPoint(id));
			}
		} finally {
			lockByKey.unlock(-1L);
		}
	}

	/**
	 * 순차적 실행 테스트 케이스 작성
	 *   - method : transactionChargeAndUse()
	 * charge -> use -> use
	 *   - 1000 charge
	 *   - 600 use
	 *   - 400 use
	 * 예상결과 : point가 1000 -> 400 -> 0 -> 1000 -> 400 -> ...  순서로 반복
	 */
	@DisplayName("PointService 멀티 스레드 테스트 - 1000 -> 400 -> 0")
	@Test
	void testMultiChargeAndUse() throws InterruptedException {

		for (int i = 0; i < MAX_EXECUTE_COUNT; i++) {
			executorService.submit(() -> {
				transactionChargeAndManyUse(1L, 1000L, 600L, 400L);
				latch.countDown();
			});
		}

		latch.await();
		assertThat(getHistoriesSize()).isEqualTo(MAX_EXECUTE_COUNT * 3);
	}

	@DisplayName("PointService 멀티 스레드 테스트 - 잔액이 부족한 경우 실행되지 않음")
	@Test
	@Disabled
	void testMultiChargeAndUse2() throws InterruptedException {
		AtomicInteger failCount = new AtomicInteger(0);

		for (int i = 0; i < MAX_EXECUTE_COUNT; i++) {
			executorService.submit(() -> {
				try {
					transactionChargeAndManyUse(1L, 1000L, 600L, 600L);
				} catch (PointException e) {
					log.warn(e.getMessage());
					failCount.getAndIncrement();
				} finally {
					latch.countDown();
				}
			});
		}

		latch.await();
		assertThat(failCount).hasValueGreaterThan(0);
		assertThat(getHistoriesSize()).isEqualTo(MAX_EXECUTE_COUNT * 3 - failCount.intValue());
	}

	@DisplayName("PointController 멀티 스레드 테스트 - 충전 반복")
	@Test
	void controllerMultiRequestCharge() throws InterruptedException {
		AtomicInteger failCount = new AtomicInteger(0);

		for (int i = 0; i < MAX_EXECUTE_COUNT; i++) {
			executorService.submit(() -> {
				try {
					mockMvc.perform(patch("/point/1/charge")
						.contentType(MediaType.APPLICATION_JSON)
						.content(makeUserPointRequest(10L, TransactionType.CHARGE)))
					  .andDo(print());
					  // .andExpect(status().isOk());
				} catch (Exception e) {
					log.warn(e.getMessage());
					failCount.getAndIncrement();
				} finally {
					latch.countDown();
				}
			});
		}

		latch.await();
		assertThat(getPoint(1L)).isEqualTo(10 * MAX_EXECUTE_COUNT);
		assertThat(failCount.intValue()).isEqualTo(0);
	}

	private String makeUserPointRequest(long amount, TransactionType type) throws JsonProcessingException {
		return objectMapper.writeValueAsString(
		  type.equals(TransactionType.CHARGE)
			? new UserPointRequest.Charge(amount)
			: new UserPointRequest.Use(amount)
		);
	}
}


