package com.example.concertreservation.domain.concert.service;

import com.example.concertreservation.common.dto.AuthUser;
import com.example.concertreservation.common.exception.InvalidRequestException;
import com.example.concertreservation.domain.concert.dto.request.ConcertSaveRequest;
import com.example.concertreservation.domain.concert.dto.request.ConcertUpdateRequest;
import com.example.concertreservation.domain.concert.dto.response.*;
import com.example.concertreservation.domain.concert.entity.Concert;
import com.example.concertreservation.domain.concert.entity.ConcertReservationDate;
import com.example.concertreservation.domain.concert.repository.ConcertRepository;
import com.example.concertreservation.domain.concert.repository.ConcertReservationDateRepository;
import com.example.concertreservation.domain.concert.util.RedisKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConcertService {

    private final ConcertRepository concertRepository;
    private final ConcertReservationDateRepository concertReservationDateRepository;
    private final RedisTemplate<String, String> redisTemplate;

    @CacheEvict(value = "concertList", allEntries = true)
    @Transactional
    public ConcertResponse saveConcert(ConcertSaveRequest saveRequest) {

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

        return new ConcertResponse(
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
    public ConcertResponse updateConcert(Long concertId, ConcertUpdateRequest request) {
        ConcertReservationDate reservationDate = getReservationDateWithConcertOrThrow(concertId);
        Concert concert = reservationDate.getConcert();

        // 업데이트할 필드 구분
        String title = request.getTitle() != null ? request.getTitle() : concert.getTitle();
        String description = request.getDescription() != null ? request.getDescription() : concert.getDescription();
        LocalDateTime concertDate = request.getConcertDate() != null ? request.getConcertDate() : concert.getConcertDate();

        int oldCapacity = concert.getCapacity();
        int oldAvailableAmount = concert.getAvailableAmount();
        int newCapacity = request.getCapacity() != null ? request.getCapacity() : oldCapacity;

        // 정원 변경이 있을 경우 availableAmount 재계산
        int diff = newCapacity - oldCapacity;
        int newAvailableAmount = oldAvailableAmount + diff;

        if (newAvailableAmount < 0) {
            throw new InvalidRequestException("이미 예매된 좌석 수보다 적은 정원으로 변경할 수 없습니다.");
        }

        // concert 업데이트
        concert.update(title, description, concertDate, newCapacity, newAvailableAmount);

        // reservationDate 업데이트 (startDate, endDate 둘 다 있어야 업데이트)
        if (request.getStartDate() != null && request.getEndDate() != null) {
            reservationDate.update(request.getStartDate(), request.getEndDate());
        }

        return new ConcertResponse(
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

    public ConcertDetailResponse getConcert(Long concertId, AuthUser authUser) {
        ConcertReservationDate reservationDate = concertReservationDateRepository
                .findByConcertIdWithConcert(concertId)
                .orElseThrow(() -> new InvalidRequestException("해당 콘서트의 예매 일정이 존재하지 않습니다."));

        Concert concert = reservationDate.getConcert();

        // 로그인 사용자 기준 어뷰징 방지
        String userViewKey = RedisKey.CONCERT_USER_VIEW + concertId + ":" + authUser.getId();
        boolean alreadyViewed = Boolean.TRUE.equals(redisTemplate.hasKey(userViewKey));

        String redisKey = RedisKey.CONCERT_VIEW_COUNT + concertId;
        if (!alreadyViewed) {
            redisTemplate.opsForValue().increment(redisKey);
            redisTemplate.opsForValue().set(userViewKey, "1", 12, TimeUnit.HOURS);
            redisTemplate.opsForZSet().incrementScore(RedisKey.CONCERT_VIEW_RANKING, concertId.toString(), 1);
        }

        int redisViewCount = Optional.ofNullable(redisTemplate.opsForValue().get(redisKey))
                .map(Integer::parseInt)
                .orElse(0);

        int totalViewCount = concert.getViewCount() + redisViewCount;

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

    public List<ConcertSummaryResponse> getPopularConcerts(int top) {
        Set<String> concertIdSet = redisTemplate.opsForZSet()
                .reverseRange(RedisKey.CONCERT_VIEW_RANKING, 0, top - 1);

        if (concertIdSet == null || concertIdSet.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> concertIds = concertIdSet.stream()
                .map(Long::parseLong)
                .toList();

        List<Concert> concerts = concertRepository.findAllById(concertIds);

        Map<Long, Concert> concertMap = concerts.stream()
                .collect(Collectors.toMap(Concert::getId, Function.identity()));

        return concertIds.stream()
                .map(id -> {
                    Concert c = concertMap.get(id);
                    return new ConcertSummaryResponse(
                            c.getId(), c.getTitle(), c.getConcertDate()
                    );
                })
                .toList();
    }

    @CacheEvict(value = "concertList", allEntries = true)
    @Transactional
    public void flushViewCountToDB() {
        Set<String> keys = redisTemplate.keys("concert:view:*");

        if (keys == null || keys.isEmpty()) {
            log.info("조회수 반영할 Redis 키 없음");
            return;
        }

        for (String key : keys) {
            String keySuffix = key.replace("concert:view:", "");
            // 숫자가 아닌 키는 스킵
            if (!keySuffix.matches("\\d+")) {
                log.warn("조회수 처리 대상 아님 - 무시된 Redis 키: {}", key);
                continue;
            }

            try {
                Long concertId = Long.parseLong(key.replace("concert:view:", ""));
                String value = redisTemplate.opsForValue().get(key);

                if (value == null) continue;

                int redisViewCount = Integer.parseInt(value);
                int currentViewCount = concertRepository.findViewCountById(concertId);
                int totalViewCount = currentViewCount + redisViewCount;

                concertRepository.updateViewCount(concertId, totalViewCount);

                redisTemplate.delete(key);
                redisTemplate.delete("concert:view:ranking");

                log.info("조회수 DB 반영 완료 - concertId: {}, viewCount: {}", concertId, totalViewCount);
            } catch (Exception e) {
                log.error("조회수 DB 반영 실패 - key: {}", key, e);
            }
        }
    }

    private Concert getConcertOrThrow(Long concertId) {
        return concertRepository.findById(concertId)
                .orElseThrow(() -> new InvalidRequestException("해당 콘서트가 존재하지 않습니다."));
    }

    private ConcertReservationDate getReservationDateWithConcertOrThrow(Long concertId) {
        return concertReservationDateRepository
                .findByConcertIdWithConcert(concertId)
                .orElseThrow(() -> new InvalidRequestException("해당 콘서트와 예매 일정이 존재하지 않습니다."));
    }
}
