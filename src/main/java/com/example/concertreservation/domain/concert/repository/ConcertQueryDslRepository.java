package com.example.concertreservation.domain.concert.repository;

import com.example.concertreservation.domain.concert.dto.response.ConcertSummaryWithoutView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface ConcertQueryDslRepository {
    Page<ConcertSummaryWithoutView> searchConcerts(
            Pageable pageable,
            String keyword,
            LocalDateTime fromDate,
            LocalDateTime toDate
    );
}
