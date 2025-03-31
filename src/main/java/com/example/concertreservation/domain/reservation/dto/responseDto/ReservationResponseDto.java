package com.example.concertreservation.domain.reservation.dto.responseDto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ReservationResponseDto {

    private final Long reservationId;
    private final Long concertId;
    private final Long userId;
}
