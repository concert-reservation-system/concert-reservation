package com.example.concertreservation.domain.concert.entity;

import com.example.concertreservation.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
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

    @Version
    private Long version; // 낙관적 락

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
        if (this.availableAmount <= 0) {
            String errorMessage = "잔여 수량이 없습니다.";
            throw new IllegalStateException(errorMessage);
        }
        this.availableAmount -= 1;
    }
}