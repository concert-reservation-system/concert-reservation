package com.example.concertreservation.domain.reservation.controller;

import com.example.concertreservation.common.dto.AuthUser;
import com.example.concertreservation.common.lock.redisson.LockReservationService;
import com.example.concertreservation.domain.reservation.dto.response.ReservationResponse;
import com.example.concertreservation.domain.reservation.dto.response.ReservationSearchResponse;
import com.example.concertreservation.domain.reservation.entity.Reservation;
import com.example.concertreservation.domain.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    private final LockReservationService lockReservationService;

    @PostMapping("/{concertId}")
    public ResponseEntity<Void> reserveConcert(@PathVariable Long concertId,
                                               @AuthenticationPrincipal AuthUser authUser) {
        try {
            lockReservationService.executeWithLock(concertId, authUser.getId());
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping()
    public ResponseEntity<Page<ReservationSearchResponse>> searchMyReservations(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String concertTitle,
            @RequestParam(required = false) LocalDateTime fromDate,
            @RequestParam(required = false) LocalDateTime toDate
    ) {
        return ResponseEntity.ok(reservationService
                .findMyReservations(authUser, page, size, concertTitle, fromDate, toDate));
    }

    public static ReservationResponse from(Reservation reservation) {
        return ReservationResponse.builder()
                .reservationId(reservation.getId())
                .concertId(reservation.getConcert().getId())
                .userId(reservation.getUser().getId())
                .createdAt(reservation.getCreatedAt())
                .build();
    }

}