package com.example.concertreservation.domain.concert.entity;

import com.example.concertreservation.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "concert_reservation_dates")
public class ConcertReservationDate extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concert_id", nullable = false, unique = true)
    private Concert concert;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
