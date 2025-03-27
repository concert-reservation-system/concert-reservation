package com.example.concertreservation.common.fairlock;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import org.springframework.context.ApplicationContext;

@Service
@RequiredArgsConstructor
public class LockReservationService {

    private final LockManager lockManager;
    private final ReservationTransactionalFacade reservationTransactionalFacade;

    public void executeWithLock(Long concertId, Long userId) {
        String lockKey = "concert:reservation:" + concertId;

        try {
            System.out.println("락 획득 시도: concertId=" + concertId + ", userId=" + userId);
            lockManager.executeWithLock(lockKey, () -> {
                try {
                    System.out.println("람다 내부 진입: userId=" + userId);
                    reservationTransactionalFacade.reserveWithTransaction(concertId, userId);
                    System.out.println("람다 내부 정상 종료: userId=" + userId);
                } catch (Exception e) {
                    System.out.println("람다 내부 예외 발생: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("락 획득 중 인터럽트 발생: " + concertId + ", userId=" + userId);
            throw new IllegalStateException("락 획득 중 인터럽트 발생: " + concertId, e);
        }
    }
}