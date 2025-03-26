package com.example.concertreservation.domain.reservation.repository;

import com.example.concertreservation.domain.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
}
