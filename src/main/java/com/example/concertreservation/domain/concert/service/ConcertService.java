package com.example.concertreservation.domain.concert.service;

import com.example.concertreservation.common.exception.InvalidRequestException;
import com.example.concertreservation.domain.concert.dto.request.ConcertReservationPeriodRequest;
import com.example.concertreservation.domain.concert.dto.request.ConcertSaveRequest;
import com.example.concertreservation.domain.concert.dto.request.ConcertUpdateRequest;
import com.example.concertreservation.domain.concert.dto.response.*;
import com.example.concertreservation.domain.concert.entity.Concert;
import com.example.concertreservation.domain.concert.entity.ConcertReservationDate;
import com.example.concertreservation.domain.concert.repository.ConcertRepository;
import com.example.concertreservation.domain.concert.repository.ConcertReservationDateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConcertService {

    private final ConcertRepository concertRepository;
    private final ConcertReservationDateRepository concertReservationDateRepository;
    private final RedisTemplate<String, String> redisTemplate;

    @CacheEvict(value = "concertList", allEntries = true)
    @Transactional
    public ConcertSaveResponse saveConcert(ConcertSaveRequest saveRequest) {

        // 콘서트 저장
        Concert newConcert = Concert.builder()
                .title(saveRequest.getTitle())
                .description(saveRequest.getDescription())
                .concertDate(saveRequest.getConcertDate())
                .capacity(saveRequest.getCapacity())
                .availableAmount(saveRequest.getCapacity())
                .build();

        Concert savedConcert = concertRepository.save(newConcert);

        // 콘서트 예매 일정 저장
        ConcertReservationDate reservationDate = ConcertReservationDate.builder()
                .concert(savedConcert)
                .startDate(saveRequest.getStartDate())
                .endDate(saveRequest.getEndDate())
                .build();
        ConcertReservationDate savedReservationDate = concertReservationDateRepository.save(reservationDate);

        return new ConcertSaveResponse(
                savedConcert.getId(),
                savedConcert.getTitle(),
                savedConcert.getDescription(),
                savedConcert.getConcertDate(),
                savedConcert.getCapacity(),
                savedReservationDate.getStartDate(),
                savedReservationDate.getEndDate()
        );
    }

    @CacheEvict(value = "concertList", allEntries = true)
    @Transactional
    public ConcertResponse updateConcert(Long concertId, ConcertUpdateRequest concertUpdateRequest) {
        Concert concert = getConcertOrThrow(concertId);

        int oldCapacity = concert.getCapacity();
        int newCapacity = concertUpdateRequest.getCapacity();
        int oldAvailableAmount = concert.getAvailableAmount();

        int diff = newCapacity - oldCapacity;
        int newAvailableAmount = oldAvailableAmount + diff;

        if (newAvailableAmount < 0) {
            throw new InvalidRequestException("이미 예매된 좌석 수보다 적은 정원으로 변경할 수 없습니다.");
        }

        concert.update(
                concertUpdateRequest.getTitle(),
                concertUpdateRequest.getDescription(),
                concertUpdateRequest.getConcertDate(),
                concertUpdateRequest.getCapacity(),
                newAvailableAmount
        );

        return new ConcertResponse(
                concert.getId(),
                concert.getTitle(),
                concert.getDescription(),
                concert.getConcertDate(),
                concert.getCapacity()
        );
    }

    @Transactional
    public ConcertSaveResponse updateReservationPeriod(Long concertId, ConcertReservationPeriodRequest concertReservationPeriodRequest) {
        Concert concert = getConcertOrThrow(concertId);

        ConcertReservationDate reservationDate = getReservationDateOrThrow(concertId);

        reservationDate.update(
                concertReservationPeriodRequest.getStartDate(),
                concertReservationPeriodRequest.getEndDate()
        );

        return new ConcertSaveResponse(
                concert.getId(),
                concert.getTitle(),
                concert.getDescription(),
                concert.getConcertDate(),
                concert.getCapacity(),
                reservationDate.getStartDate(),
                reservationDate.getEndDate()
        );
    }

    @CacheEvict(value = "concertList", allEntries = true)
    @Transactional
    public void deleteConcert(Long concertId) {
        Concert concert = getConcertOrThrow(concertId);

        // 콘서트 예매 일정 삭제
        concertReservationDateRepository.findByConcertId(concertId)
                .ifPresent(concertReservationDateRepository::delete);

        // 콘서트 삭제
        concertRepository.delete(concert);
    }

    public ConcertDetailResponse getConcert(Long concertId) {
        ConcertReservationDate reservationDate = concertReservationDateRepository
                .findByConcertIdWithConcert(concertId)
                .orElseThrow(() -> new InvalidRequestException("해당 콘서트의 예매 일정이 존재하지 않습니다."));

        Concert concert = reservationDate.getConcert();

        String redisKey = "concert:view:" + concertId;
        Long redisViewCount = redisTemplate.opsForValue().increment(redisKey);
        int additionalViews = (redisViewCount != null) ? redisViewCount.intValue() : 0;
        int totalViewCount = concert.getViewCount() + additionalViews;

        return new ConcertDetailResponse(
                concert.getId(),
                concert.getTitle(),
                concert.getDescription(),
                concert.getConcertDate(),
                concert.getCapacity(),
                concert.getAvailableAmount(),
                totalViewCount,
                reservationDate.getStartDate(),
                reservationDate.getEndDate()
        );
    }

    @Cacheable(
            value = "concertList",
            key = "T(com.example.concertreservation.domain.concert.util.CacheKeyGenerator).generateConcertListKey(#page, #size, #keyword, #fromDate, #toDate)"
    )
    public Page<ConcertSummaryResponse> getConcerts(
            int page, int size, String keyword,
            LocalDateTime fromDate, LocalDateTime toDate
    ) {
        Pageable pageable = PageRequest.of(page - 1, size);
        return concertRepository.searchConcerts(pageable, keyword, fromDate, toDate);
    }

    private Concert getConcertOrThrow(Long concertId) {
        return concertRepository.findById(concertId)
                .orElseThrow(() -> new InvalidRequestException("해당 콘서트가 존재하지 않습니다."));
    }

    private ConcertReservationDate getReservationDateOrThrow(Long concertId) {
        return concertReservationDateRepository
                .findByConcertId(concertId)
                .orElseThrow(() -> new InvalidRequestException("해당 콘서트의 예매 일정이 존재하지 않습니다."));
    }
}
