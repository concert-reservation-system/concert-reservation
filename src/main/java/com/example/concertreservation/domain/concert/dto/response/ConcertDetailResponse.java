package com.example.concertreservation.domain.concert.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ConcertDetailResponse {

    private final Long id;
    private final String title;
    private final String description;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime concertDate;
    private final int capacity;
    private final int availableAmount;
    private final int viewCount;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime startDate;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime endDate;

    public ConcertDetailResponse(Long id, String title, String description, LocalDateTime concertDate, int capacity,
                                 int availableAmount, int viewCount, LocalDateTime startDate, LocalDateTime endDate) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.concertDate = concertDate;
        this.capacity = capacity;
        this.availableAmount = availableAmount;
        this.viewCount = viewCount;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
