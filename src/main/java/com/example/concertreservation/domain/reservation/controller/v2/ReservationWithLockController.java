package com.example.concertreservation.domain.reservation.controller.v2;

import com.example.concertreservation.common.lock.redisson.LockReservationService;
import com.example.concertreservation.domain.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    private final LockReservationService lockReservationService;

    @PostMapping("/{concertId}")
    public ResponseEntity<Void> reserveConcert(@PathVariable Long concertId,
                                               @RequestParam Long userId) {
        try {
            lockReservationService.executeWithLock(concertId, userId);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}