package com.example.concertreservation.domain.reservation.repository;

import com.example.concertreservation.domain.reservation.dto.response.ReservationSearchResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static com.example.concertreservation.domain.reservation.entity.QReservation.reservation;

@Repository
@RequiredArgsConstructor
public class ReservationQueryDslRepositoryImpl implements ReservationQueryDslRepository {

    private final JPAQueryFactory queryFactory;
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<ReservationSearchResponse> findMyReservations(Pageable pageable, Long userId, String concertTitle, LocalDateTime fromDate, LocalDateTime toDate) {
        List<ReservationSearchResponse> reservations = jpaQueryFactory.select(Projections.constructor(
                        ReservationSearchResponse.class,
                        reservation.id,
                        reservation.concert.id,
                        reservation.concert.title,
                        reservation.concert.concertDate,
                        reservation.createdAt
                ))
                .from(reservation)
                .join(reservation.concert)
                .join(reservation.user)
                .where(
                        reservationUserIdEq(userId),
                        reservationConcertTitleContains(concertTitle),
                        reservationConcertFromDateGoe(fromDate),
                        reservationConcertToDateLoe(toDate)
                )
                .orderBy(reservation.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long totalSize = queryFactory.select(Wildcard.count)
                .from(reservation)
                .where(
                        reservationUserIdEq(userId),
                        reservationConcertTitleContains(concertTitle),
                        reservationConcertFromDateGoe(fromDate),
                        reservationConcertToDateLoe(toDate)
                )
                .fetchOne();

        return PageableExecutionUtils.getPage(reservations, pageable, () -> totalSize);
    }

    private BooleanExpression reservationUserIdEq(Long userId) {
        return reservation.user.id.eq(userId);
    }

    private BooleanExpression reservationConcertTitleContains(String concertTitle) {
        return StringUtils.isEmpty(concertTitle) ? null : reservation.concert.title.like("%" + concertTitle + "%");
    }

    private BooleanExpression reservationConcertFromDateGoe(LocalDateTime startDate) {
        return Objects.nonNull(startDate) ? reservation.concert.concertDate.goe(startDate) : null;
    }

    private BooleanExpression reservationConcertToDateLoe(LocalDateTime endDate) {
        return Objects.nonNull(endDate) ? reservation.concert.concertDate.loe(endDate) : null;
    }
}

