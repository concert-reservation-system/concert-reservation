package com.example.concertreservation.domain.concert.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class ConcertUpdateRequest {

    private String title;

    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime concertDate;

    private Integer capacity;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endDate;

    @Builder
    public ConcertUpdateRequest(String title, String description, LocalDateTime concertDate,
                                 Integer capacity, LocalDateTime startDate, LocalDateTime endDate) {
        this.title = title;
        this.description = description;
        this.concertDate = concertDate;
        this.capacity = capacity;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
