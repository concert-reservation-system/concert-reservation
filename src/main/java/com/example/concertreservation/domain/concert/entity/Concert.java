package com.example.concertreservation.domain.concert.entity;

import com.example.concertreservation.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "concerts")
public class Concert extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    @Column(columnDefinition = "TEXT")
    private String description;
    private LocalDateTime concertDate;
    private Integer capacity;
    private Integer viewCount = 0;

    public Concert(String title, String description, LocalDateTime concertDate, Integer capacity) {
        this.title = title;
        this.description = description;
        this.concertDate = concertDate;
        this.capacity = capacity;
    }
}
