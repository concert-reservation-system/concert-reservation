package com.example.concertreservation.domain.concert.repository;

import com.example.concertreservation.domain.concert.entity.Concert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ConcertRepository extends JpaRepository<Concert, Long>, ConcertQueryDslRepository {

    @Query("SELECT c.viewCount FROM Concert c WHERE c.id = :concertId")
    int findViewCountById(@Param("concertId") Long concertId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Concert c SET c.viewCount = :viewCount WHERE c.id = :concertId")
    void updateViewCount(@Param("concertId") Long concertId, @Param("viewCount") int viewCount);
}
