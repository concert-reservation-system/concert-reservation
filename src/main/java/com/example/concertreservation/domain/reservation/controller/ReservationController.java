package com.example.concertreservation.domain.reservation.controller;

import com.example.concertreservation.common.dto.AuthUser;
import com.example.concertreservation.common.annotation.LoginUser;
import com.example.concertreservation.domain.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    // 콘서트 예약 등록
    @PostMapping("/{concertId}")
    public ResponseEntity<String> createReservation(@PathVariable Long concertId, @LoginUser AuthUser authUser) {
        // 로그인 검증
        if (authUser == null) {
            return ResponseEntity.status(403).body("로그인이 필요합니다.");
        }

        // 예약 실행
        Long userId = authUser.getId();
        reservationService.createReservation(concertId, userId);

        return ResponseEntity.ok("예약이 완료되었습니다.");
    }
}
