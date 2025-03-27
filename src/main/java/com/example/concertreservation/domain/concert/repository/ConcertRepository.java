package com.example.concertreservation.domain.concert.repository;

import com.example.concertreservation.domain.concert.entity.Concert;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ConcertRepository extends JpaRepository<Concert, Long>, ConcertQueryDslRepository {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Concert c where c.id = :id")
    Optional<Concert> findByIdWithPessimisticLock(Long id);

    @Lock(LockModeType.OPTIMISTIC)
    @Query("select c from Concert c where c.id = :id")
    Optional<Concert> findByIdWithOptimisticLock(Long id);
}
