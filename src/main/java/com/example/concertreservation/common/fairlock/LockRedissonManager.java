package com.example.concertreservation.common.fairlock;

import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class LockRedissonManager implements LockManager {

    private final RedissonClient redissonClient;
    private static final String LOCK_KEY_PREFIX = "concert-reservation-lock:";
    private static final long WAIT_TIME = 10;
    private static final long LEASE_TIME = 2;

    @Override
    public void executeWithLock(String key, Runnable task) throws InterruptedException {
        String lockKey = LOCK_KEY_PREFIX + key;
        RLock lock = redissonClient.getFairLock(lockKey);

        System.out.println("락 획득 시도: " + lockKey);

        if (lock.tryLock(WAIT_TIME, LEASE_TIME, TimeUnit.SECONDS)) {
            try {
                System.out.println("락 획득 성공: " + lockKey);
                task.run();
            } catch (Exception e) {
                System.out.println("람다 내부에서 예외 발생: " + e.getMessage());
                e.printStackTrace();
            } finally {
                lock.unlock();
                System.out.println("락 해제: " + lockKey);
            }
        } else {
            System.out.println("락 획득 실패: " + lockKey);
            throw new IllegalStateException("[LockExecutor] 락 획득 실패: " + lockKey);
        }
    }
}