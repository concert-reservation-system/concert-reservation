package com.example.concertreservation.domain.concert.repository;

import com.example.concertreservation.domain.concert.entity.Concert;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConcertRepository extends JpaRepository<Concert, Long>, ConcertQueryDslRepository {
}
