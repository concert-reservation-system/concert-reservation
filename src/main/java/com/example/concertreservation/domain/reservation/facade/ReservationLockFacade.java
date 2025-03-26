package com.example.concertreservation.domain.reservation.facade;

import com.example.concertreservation.common.fairlock.LockManager;
import com.example.concertreservation.domain.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReservationLockFacade {

    private final LockManager lockExecutor;
    private final ReservationService reservationService; // 아직 service 구현 안됨

    private static final String LOCK_KEY_PREFIX = "concert:reservation:";

    public void executeWithLock(Long concertId, Runnable task) {
        String lockKey = LOCK_KEY_PREFIX + concertId;

        try {
            lockExecutor.execute(lockKey, () -> reservationService.reserve(concertId, userId));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("[ReservationLockFacade] 락 휙득 중 인터럽트 발생: " + concertId, e);
        }
    }
}