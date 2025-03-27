package com.example.concertreservation.domain.concert.dto.response;

import lombok.Getter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
public class ConcertSummaryWithoutView implements Serializable {
    private final Long id;
    private final String title;
    private final LocalDateTime concertDate;

    public ConcertSummaryWithoutView(Long id, String title, LocalDateTime concertDate) {
        this.id = id;
        this.title = title;
        this.concertDate = concertDate;
    }
}
