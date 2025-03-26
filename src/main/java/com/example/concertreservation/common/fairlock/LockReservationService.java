package com.example.concertreservation.common.fairlock;

import com.example.concertreservation.domain.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LockReservationService {

    private final LockManager lockManager;
    private final ReservationService reservationService;

    public void executeWithLock(Long concertId, Long userId) {
        String lockKey = "concert:reservation:" + concertId;

        try {
            lockManager.executeWithLock(lockKey, () -> reservationService.reserve(concertId, userId));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("락 획득 중 인터럽트 발생: " + concertId, e);
        }
    }
}
