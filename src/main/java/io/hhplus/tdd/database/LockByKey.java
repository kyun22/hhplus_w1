package io.hhplus.tdd.database;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.stereotype.Component;

/**
 * Acquire Lock By Key
 *   - 내부 스태틱 클래스로 ReentrantLock을 래핑 하는 LockWrapper 작성
 *     - ReentrantLock
 *     - AtomicValue (integer)
 *   - ConcurrentHashMap 스태틱 필드로 가지며 lock 처리시 used key 여부를 판단
 *
 *   작동 순서
 *   1. try - LockByKey.lock()
 *     - 해당 키가 이미 존재하는 경우 큐에 스레드큐 카운트를 1증가
 *     - 해당 키가 존재하지 않는 경우 ConcurrenthasMap에 새로운 lock저장 (key = key, value = LockWrapper)
 *   2. 동기화가 필요한 비즈니스 로직 실행
 *   3. finally - LockByKey.unlock()
 *     - 스레드큐 카운트를 1감소 --> 카운트가 0인 경우 해시맵에서 제거
 *
 *   주의 : try-finally 사용해야 실행중 예외가 발생해도 unlock()할수 있다.
 */
@Component
public class LockByKey {
	private static class LockWrapper {
		private final Lock lock = new ReentrantLock(true);
		private final AtomicInteger numberOfThreadsInQueue = new AtomicInteger(1);

		private LockWrapper addThreadInQueue() {
			numberOfThreadsInQueue.incrementAndGet();
			return this;
		}

		private int removeThreadFromQueue() {
			return numberOfThreadsInQueue.decrementAndGet();
		}

	}

	private static ConcurrentHashMap<Long, LockWrapper> locks = new ConcurrentHashMap<Long, LockWrapper>();

	public void lock(Long key) {
		LockWrapper lockWrapper = locks.compute(key, (k, v) -> v == null ? new LockWrapper() : v.addThreadInQueue());
		lockWrapper.lock.lock();
	}

	public void unlock(Long key) {
		LockWrapper lockWrapper = locks.get(key);
		lockWrapper.lock.unlock();
		if (lockWrapper.removeThreadFromQueue() == 0) {
			locks.remove(key, lockWrapper);
		}
	}
}
