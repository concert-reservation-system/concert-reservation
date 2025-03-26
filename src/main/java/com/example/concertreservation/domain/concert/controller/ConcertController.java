package com.example.concertreservation.domain.concert.controller;

import com.example.concertreservation.domain.concert.dto.request.ConcertSaveRequest;
import com.example.concertreservation.domain.concert.dto.response.ConcertSaveResponse;
import com.example.concertreservation.domain.concert.service.ConcertService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/concert")
@RequiredArgsConstructor
public class ConcertController {

    private final ConcertService concertService;

    @PostMapping()
    public ResponseEntity<ConcertSaveResponse> saveConcert(
            @Valid @RequestBody ConcertSaveRequest concertSaveRequest
    ) {
        return ResponseEntity.ok(concertService.saveConcert(concertSaveRequest));
    }
}
