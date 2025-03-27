package com.example.concertreservation.domain.reservation.controller;

import com.example.concertreservation.common.dto.AuthUser;
import com.example.concertreservation.domain.reservation.dto.responseDto.ReservationResponseDto;
import com.example.concertreservation.domain.reservation.entity.Reservation;
import com.example.concertreservation.domain.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    public ResponseEntity<String> createReservation(@PathVariable Long concertId) {

        // 현재 로그인한 사용자 정보 가져오기, 로그인 검증 => @AuthUser 애노테이션 사용하는 방향으로 refactor 계획
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthUser authUser)) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

        Long userId = authUser.getId();
        reservationService.createReservation(concertId, userId);

        return ResponseEntity.ok("예약이 완료되었습니다.");
    }
}
