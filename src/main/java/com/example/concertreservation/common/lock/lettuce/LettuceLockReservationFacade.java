package com.example.concertreservation.common.lock.lettuce;

import com.example.concertreservation.domain.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LettuceLockReservationFacade {

    private final RedisLockRepository redisLockRepository;
    private final ReservationService reservationService;

    public void create(Long concertId, Long userId) throws InterruptedException {
        while (!redisLockRepository.lock(concertId)) {
            // 재시도 시 thread sleep 하여 부하 감소
            Thread.sleep(100);
        }
        try {
            reservationService.createReservation(concertId, userId);
        } finally {
            redisLockRepository.unlock(concertId);
        }
    }
}
