package com.example.concertreservation.domain.concert.service;

import com.example.concertreservation.domain.concert.dto.response.ConcertSummaryWithoutView;
import com.example.concertreservation.domain.concert.repository.ConcertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ConcertCacheService {

    private final ConcertRepository concertRepository;

    @Cacheable(
            value = "concertList",
            key = "T(com.example.concertreservation.domain.concert.util.CacheKeyGenerator).generateConcertListKey(#page, #size, #keyword, #fromDate, #toDate)"
    )
    public Page<ConcertSummaryWithoutView> getCachedConcertList(
            int page, int size, String keyword,
            LocalDateTime fromDate, LocalDateTime toDate
    ) {
        Pageable pageable = PageRequest.of(page - 1, size);
        return concertRepository.searchConcerts(pageable, keyword, fromDate, toDate);
    }
}