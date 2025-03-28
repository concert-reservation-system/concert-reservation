package com.example.concertreservation.domain.concert.repository;

import com.example.concertreservation.domain.concert.entity.Concert;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;

public interface ConcertRepository extends JpaRepository<Concert, Long>, ConcertQueryDslRepository {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Concert c where c.id = :id")
    Optional<Concert> findByIdWithPessimisticLock(Long id);

    @Lock(LockModeType.OPTIMISTIC)
    @Query("select c from Concert c where c.id = :id")
    Optional<Concert> findByIdWithOptimisticLock(Long id);

    @Query("SELECT c.viewCount FROM Concert c WHERE c.id = :concertId")
    int findViewCountById(@Param("concertId") Long concertId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Concert c SET c.viewCount = :viewCount WHERE c.id = :concertId")
    void updateViewCount(@Param("concertId") Long concertId, @Param("viewCount") int viewCount);

    @Modifying
    @Query("UPDATE Concert c SET c.availableAmount = c.availableAmount + :delta WHERE c.id = :concertId")
    void updateAvailableAmount(@Param("concertId") Long concertId, @Param("delta") int delta);
}
