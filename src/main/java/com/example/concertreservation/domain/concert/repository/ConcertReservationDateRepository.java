package com.example.concertreservation.domain.concert.repository;

import com.example.concertreservation.domain.concert.entity.ConcertReservationDate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConcertReservationDateRepository extends JpaRepository<ConcertReservationDate, Long>  {
}
