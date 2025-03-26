package com.example.concertreservation.domain.concert.dto.response;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ConcertResponse {
    private final Long id;
    private final String title;
    private final String description;
    private final LocalDateTime concertDate;
    private final int capacity;

    public ConcertResponse(Long id, String title, String description, LocalDateTime concertDate, int capacity) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.concertDate = concertDate;
        this.capacity = capacity;
    }
}
