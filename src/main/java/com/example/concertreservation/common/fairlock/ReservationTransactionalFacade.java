package com.example.concertreservation.common.fairlock;

import com.example.concertreservation.domain.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReservationTransactionalFacade {

    private final ReservationService reservationService;

    @Transactional
    public void reserveWithTransaction(Long concertId, Long userId) {
        reservationService.createReservation(concertId, userId);
    }
}