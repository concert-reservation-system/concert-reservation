package com.example.concertreservation.domain.reservation.entity;

import com.example.concertreservation.common.entity.BaseTimeEntity;
import com.example.concertreservation.domain.concert.entity.Concert;
import com.example.concertreservation.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "reservations")
public class Reservation extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concert_id", nullable = false)
    private Concert concert;

    @Builder
    public Reservation(User user, Concert concert) {
        this.user = user;
        this.concert = concert;
    }
}
