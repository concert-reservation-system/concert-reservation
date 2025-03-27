package com.example.concertreservation.common.lock.aop;

import com.example.concertreservation.common.exception.LockAcquisitionException;
import com.example.concertreservation.common.lock.aop.annotation.RedisLock;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
@RequiredArgsConstructor
public class RedisLockAspect {

    private final RedissonClient redissonClient;

    @Around("@annotation(com.example.concertreservation.common.lock.aop.annotation.RedisLock)")
    public Object aopLock(ProceedingJoinPoint joinPoint) throws Throwable {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RedisLock redisLock = method.getAnnotation(RedisLock.class);

        String key = "redis_lock_" + redisLock.key();
        RLock lock = redissonClient.getLock(key);

        if (lock.tryLock(5L, TimeUnit.SECONDS)) {
            try {
                return joinPoint.proceed();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new LockAcquisitionException("락 획득 중 인터럽트 발생");
            } finally {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        } else {
            throw new IllegalStateException("락 획득 실패");
        }
    }
}
