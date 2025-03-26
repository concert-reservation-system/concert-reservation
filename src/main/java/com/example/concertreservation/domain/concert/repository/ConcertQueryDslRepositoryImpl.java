package com.example.concertreservation.domain.concert.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ConcertQueryDslRepositoryImpl implements ConcertQueryDslRepository {

    private final JPAQueryFactory queryFactory;
}