package com.example.concertreservation.domain.concert.repository;

import com.example.concertreservation.domain.concert.entity.ConcertReservationDate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConcertReservationDateRepository extends JpaRepository<ConcertReservationDate, Long>  {
    Optional<ConcertReservationDate> findByConcertId(Long concertId);
}
