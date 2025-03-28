package com.example.concertreservation.domain.concert.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ConcertResponse {

    private final Long id;
    private final String title;
    private final String description;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime concertDate;
    private final int capacity;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime startDate;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime endDate;

    @Builder
    public ConcertResponse(Long id, String title, String description, LocalDateTime concertDate, int capacity, LocalDateTime startDate, LocalDateTime endDate) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.concertDate = concertDate;
        this.capacity = capacity;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
