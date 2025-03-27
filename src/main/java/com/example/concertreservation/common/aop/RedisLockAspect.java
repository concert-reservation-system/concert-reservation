package com.example.concertreservation.common.aop;

import com.example.concertreservation.common.annotation.RedisLock;
import com.example.concertreservation.common.exception.InvalidRequestException;
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

    @Around("@annotation(com.example.concertreservation.common.annotation.RedisLock)")
    public Object aopLock(ProceedingJoinPoint joinPoint) throws Throwable {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RedisLock redisLock = method.getAnnotation(RedisLock.class);

        String key = "redis_lock_" + redisLock.key();
        RLock lock = redissonClient.getLock(key);

        if (lock.tryLock(5L, TimeUnit.SECONDS)) {
            try {
                return joinPoint.proceed();
            } finally {
                lock.unlock();  // 예외처리 추가
            }
        } else {
            throw new IllegalStateException("락 획득 실패");
        }
    }
}
