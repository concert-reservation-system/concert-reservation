package com.example.concertreservation.domain.concert.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
public class ConcertSummaryResponse implements Serializable {
    private final Long id;
    private final String title;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime concertDate;

    public ConcertSummaryResponse(Long id, String title, LocalDateTime concertDate) {
        this.id = id;
        this.title = title;
        this.concertDate = concertDate;
    }
}
