package com.example.concertreservation.domain.concert.entity;

import com.example.concertreservation.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Builder;
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
    private int capacity;
    private int availableAmount;
    private int viewCount = 0;

    @Builder
    public Concert(String title, String description, LocalDateTime concertDate, int capacity, int availableAmount, int viewCount) {
        this.title = title;
        this.description = description;
        this.concertDate = concertDate;
        this.capacity = capacity;
        this.availableAmount = availableAmount;
        this.viewCount = viewCount;
    }

    public void update(String title, String description, LocalDateTime concertDate, int capacity, int availableAmount) {
        this.title = title;
        this.description = description;
        this.concertDate = concertDate;
        this.capacity = capacity;
        this.availableAmount = availableAmount;
    }

    public void decreaseAvailableAmount() {
        this.availableAmount--;
    }
}