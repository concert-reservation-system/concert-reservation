package com.example.concertreservation.common.lock.redisson;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LockReservationService {

    private final LockManager lockManager;
    private final ReservationTransactionalFacade reservationTransactionalFacade;

    public void executeWithLock(Long concertId, Long userId) {
        String lockKey = "concert:reservation:" + concertId;

        try {
            lockManager.executeWithLock(lockKey, () -> {
                reservationTransactionalFacade.reserveWithTransaction(concertId, userId);
            });
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("락 획득 중 인터럽트 발생: " + concertId, e);
        }
    }
}