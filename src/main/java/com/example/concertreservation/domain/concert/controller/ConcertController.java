package com.example.concertreservation.domain.concert.controller;

import com.example.concertreservation.common.enums.UserRole;
import com.example.concertreservation.domain.concert.dto.request.ConcertReservationPeriodRequest;
import com.example.concertreservation.domain.concert.dto.request.ConcertSaveRequest;
import com.example.concertreservation.domain.concert.dto.request.ConcertUpdateRequest;
import com.example.concertreservation.domain.concert.dto.response.ConcertResponse;
import com.example.concertreservation.domain.concert.dto.response.ConcertSaveResponse;
import com.example.concertreservation.domain.concert.service.ConcertService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

@Secured(UserRole.Authority.ADMIN)
@RestController
@RequestMapping("/api/concerts")
@RequiredArgsConstructor
public class ConcertController {

    private final ConcertService concertService;

    @PostMapping()
    public ResponseEntity<ConcertSaveResponse> saveConcert(
            @Valid @RequestBody ConcertSaveRequest concertSaveRequest
    ) {
        return ResponseEntity.ok(concertService.saveConcert(concertSaveRequest));
    }

    @PatchMapping("/{concertId}")
    public ResponseEntity<ConcertResponse> updateConcert(
            @Valid @RequestBody ConcertUpdateRequest concertUpdateRequest,
            @PathVariable long concertId
    ) {
        return ResponseEntity.ok(concertService.updateConcert(concertId, concertUpdateRequest));
    }

    @PatchMapping("/{concertId}/reservation-period")
    public ResponseEntity<ConcertSaveResponse> updateReservationPeriod(
            @Valid @RequestBody ConcertReservationPeriodRequest concertReservationPeriodRequest,
            @PathVariable long concertId
    ) {
        return ResponseEntity.ok(concertService.updateReservationPeriod(concertId, concertReservationPeriodRequest));
    }

}
