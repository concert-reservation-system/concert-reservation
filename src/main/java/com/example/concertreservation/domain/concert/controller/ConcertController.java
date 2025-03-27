package com.example.concertreservation.domain.concert.controller;

import com.example.concertreservation.common.enums.UserRole;
import com.example.concertreservation.domain.concert.dto.request.ConcertReservationPeriodRequest;
import com.example.concertreservation.domain.concert.dto.request.ConcertSaveRequest;
import com.example.concertreservation.domain.concert.dto.request.ConcertUpdateRequest;
import com.example.concertreservation.domain.concert.dto.response.ConcertDetailResponse;
import com.example.concertreservation.domain.concert.dto.response.ConcertResponse;
import com.example.concertreservation.domain.concert.dto.response.ConcertSaveResponse;
import com.example.concertreservation.domain.concert.dto.response.ConcertSummaryResponse;
import com.example.concertreservation.domain.concert.service.ConcertService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/concerts")
@RequiredArgsConstructor
public class ConcertController {

    private final ConcertService concertService;

    @Secured(UserRole.Authority.ADMIN)
    @PostMapping()
    public ResponseEntity<ConcertSaveResponse> saveConcert(
            @Valid @RequestBody ConcertSaveRequest concertSaveRequest
    ) {
        return ResponseEntity.ok(concertService.saveConcert(concertSaveRequest));
    }

    @Secured(UserRole.Authority.ADMIN)
    @PatchMapping("/{concertId}")
    public ResponseEntity<ConcertResponse> updateConcert(
            @Valid @RequestBody ConcertUpdateRequest concertUpdateRequest,
            @PathVariable long concertId
    ) {
        return ResponseEntity.ok(concertService.updateConcert(concertId, concertUpdateRequest));
    }

    @Secured(UserRole.Authority.ADMIN)
    @PatchMapping("/{concertId}/reservation-period")
    public ResponseEntity<ConcertSaveResponse> updateReservationPeriod(
            @Valid @RequestBody ConcertReservationPeriodRequest concertReservationPeriodRequest,
            @PathVariable long concertId
    ) {
        return ResponseEntity.ok(concertService.updateReservationPeriod(concertId, concertReservationPeriodRequest));
    }

    @Secured(UserRole.Authority.ADMIN)
    @DeleteMapping("/{concertId}")
    public ResponseEntity<String> deleteConcert(@PathVariable long concertId) {
        concertService.deleteConcert(concertId);
        return ResponseEntity.ok("콘서트 삭제 성공");
    }

    @GetMapping("/{concertId}")
    public ResponseEntity<ConcertDetailResponse> getConcert(@PathVariable long concertId) {
        return ResponseEntity.ok(concertService.getConcert(concertId));
    }

    @GetMapping()
    public ResponseEntity<Page<ConcertSummaryResponse>> getConcerts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) LocalDateTime fromDate,
            @RequestParam(required = false) LocalDateTime toDate
    ) {
        return ResponseEntity.ok(concertService.getConcerts(page, size, keyword, fromDate, toDate));
    }
}
