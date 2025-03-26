package com.example.concertreservation.domain.reservation.dto.response;

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
    private LocalDateTime updatedAt;
}