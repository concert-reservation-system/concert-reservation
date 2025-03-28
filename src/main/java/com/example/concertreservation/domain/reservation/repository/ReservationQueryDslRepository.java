package com.example.concertreservation.domain.reservation.repository;

import com.example.concertreservation.domain.reservation.dto.response.ReservationSearchResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface ReservationQueryDslRepository {
    Page<ReservationSearchResponse> findMyReservations(Pageable pageable,
                                                       Long userId,
                                                       String concertTitle,
                                                       LocalDateTime fromDate,
                                                       LocalDateTime toDate);
}

