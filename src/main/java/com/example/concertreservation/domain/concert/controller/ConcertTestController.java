package com.example.concertreservation.domain.concert.controller;

import com.example.concertreservation.common.enums.UserRole;
import com.example.concertreservation.domain.concert.service.ConcertDataCreator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/concerts/test")
@RequiredArgsConstructor
public class ConcertTestController {

    private final ConcertDataCreator concertDataCreator;

    @Secured(UserRole.Authority.ADMIN)
    @PostMapping("/data")
    public ResponseEntity<Void> createTestConcerts() {
        concertDataCreator.createConcerts();
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
