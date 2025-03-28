package com.example.concertreservation.common.lock.reentrant;

import com.example.concertreservation.domain.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
@RequiredArgsConstructor
public class ReentrantReservationService {

    private final ReservationService reservationService;
    private final Lock lock = new ReentrantLock();

    public void createReservation(Long concertId, Long userId) {
        lock.lock();
        try {
            reservationService.createReservation(concertId, userId);
        } finally {
            lock.unlock();
        }
    }
}
