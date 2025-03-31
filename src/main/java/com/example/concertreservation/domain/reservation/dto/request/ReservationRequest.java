package com.example.concertreservation.domain.reservation.dto.request;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReservationRequest {
    private Long concertId;
    private Long userId;
}