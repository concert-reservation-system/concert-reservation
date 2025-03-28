package com.example.concertreservation.domain.reservation.dto.response;

import com.example.concertreservation.domain.reservation.entity.Reservation;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationResponse {
    private Long reservationId;
    private Long concertId;
    private Long userId;
    private LocalDateTime createdAt;

    public static ReservationResponse from(Reservation reservation) {
        return ReservationResponse.builder()
                .reservationId(reservation.getId())
                .concertId(reservation.getConcert().getId())
                .userId(reservation.getUser().getId())
                .createdAt(reservation.getCreatedAt())
                .build();
    }
}