package com.example.concertreservation.domain.concert.dto.response;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ConcertSummaryResponse {
    private final Long id;
    private final String title;
    private final LocalDateTime concertDate;
    private final int viewCount;

    public ConcertSummaryResponse(Long id, String title, LocalDateTime concertDate, int viewCount) {
        this.id = id;
        this.title = title;
        this.concertDate = concertDate;
        this.viewCount = viewCount;
    }
}
