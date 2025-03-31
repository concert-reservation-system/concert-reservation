package com.example.concertreservation.domain.reservation.dto.response;

import com.example.concertreservation.domain.reservation.entity.Reservation;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ReservationSearchResponse {
    private final Long reservationId;
    private final Long concertId;
    private final String concertTitle;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime concertDate;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime createdAt;

    @Builder
    public ReservationSearchResponse(Long reservationId, Long concertId, String concertTitle, LocalDateTime concertDate, LocalDateTime createdAt) {
        this.reservationId = reservationId;
        this.concertId = concertId;
        this.concertTitle = concertTitle;
        this.concertDate = concertDate;
        this.createdAt = createdAt;
    }

    public static ReservationSearchResponse from(Reservation reservation) {
        return ReservationSearchResponse.builder()
                .reservationId(reservation.getId())
                .concertId(reservation.getConcert().getId())
                .concertTitle(reservation.getConcert().getTitle())
                .concertDate(reservation.getConcert().getConcertDate())
                .createdAt(reservation.getCreatedAt())
                .build();
    }
}

