package com.example.concertreservation.domain.concert.repository;

import com.example.concertreservation.domain.concert.entity.ConcertReservationDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ConcertReservationDateRepository extends JpaRepository<ConcertReservationDate, Long>  {
    Optional<ConcertReservationDate> findByConcertId(Long concertId);
    @Query("SELECT d FROM ConcertReservationDate d JOIN FETCH d.concert WHERE d.concert.id = :concertId")
    Optional<ConcertReservationDate> findByConcertIdWithConcert(@Param("concertId") Long concertId);
}
