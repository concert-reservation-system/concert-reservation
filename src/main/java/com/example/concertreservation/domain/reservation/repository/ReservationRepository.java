package com.example.concertreservation.domain.reservation.repository;

import com.example.concertreservation.domain.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    Optional<Reservation> findByUserIdAndConcertId(Long userId, Long concertId);

    long countByConcertId(Long concertId);
}
