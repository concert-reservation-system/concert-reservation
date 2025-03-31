package com.example.concertreservation.domain.concert.repository;

import com.example.concertreservation.domain.concert.dto.response.ConcertSummaryResponse;
import com.example.concertreservation.domain.concert.entity.QConcert;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ConcertQueryDslRepositoryImpl implements ConcertQueryDslRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<ConcertSummaryResponse> searchConcerts(Pageable pageable, String keyword, LocalDateTime fromDate, LocalDateTime toDate) {
        QConcert concert = QConcert.concert;
        BooleanBuilder builder = new BooleanBuilder();

        if (keyword != null && !keyword.isBlank()) {
            builder.and(concert.title.containsIgnoreCase(keyword));
        }
        if (fromDate != null) {
            builder.and(concert.concertDate.goe(fromDate));
        }
        if (toDate != null) {
            builder.and(concert.concertDate.loe(toDate));
        }

        List<ConcertSummaryResponse> content = queryFactory
                .select(Projections.constructor(
                        ConcertSummaryResponse.class,
                        concert.id,
                        concert.title,
                        concert.concertDate
                ))
                .from(concert)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(concert.concertDate.asc())
                .fetch();

        Long totalCount = queryFactory
                .select(Wildcard.count)
                .from(concert)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(content, pageable, totalCount != null ? totalCount : 0L);
    }
}