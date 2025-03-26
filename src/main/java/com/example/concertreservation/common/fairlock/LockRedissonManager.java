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

    private static final long WAIT_TIME = 10;  // 락 대기 시간 (초)
    private static final long LEASE_TIME = 2;  // 락 점유 시간 (초)

    @Override
    public void executeWithLock(String key, Runnable task) throws InterruptedException {
        String lockKey = LOCK_KEY_PREFIX + key;
        RLock lock = redissonClient.getFairLock(lockKey);

        if (lock.tryLock(WAIT_TIME, LEASE_TIME, TimeUnit.SECONDS)) {
            try {
                task.run();
            } finally {
                lock.unlock();
            }
        } else {
            throw new IllegalStateException("[LockExecutor] 락 획득 실패: " + lockKey);
        }
    }
}
