package com.example.concertreservation.domain.concert.dto.response;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ConcertSaveResponse {

    private final Long id;
    private final String title;
    private final String description;
    private final LocalDateTime concertDate;
    private final int capacity;
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;

    public ConcertSaveResponse(Long id, String title, String description, LocalDateTime concertDate, int capacity, LocalDateTime startDate, LocalDateTime endDate) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.concertDate = concertDate;
        this.capacity = capacity;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
