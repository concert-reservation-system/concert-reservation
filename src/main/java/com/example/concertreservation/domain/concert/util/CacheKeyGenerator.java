package com.example.concertreservation.domain.concert.util;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Slf4j
public class CacheKeyGenerator {
    public static String generateConcertListKey(int page, int size, String keyword, LocalDateTime fromDate, LocalDateTime toDate) {
        String key = String.format(
                "keyword=%s:from=%s:to=%s:page=%d:size=%d",
                keyword != null ? keyword : "",
                fromDate != null ? fromDate.toString() : "",
                toDate != null ? toDate.toString() : "",
                page,
                size
        );
        log.info("캐시 키: {}", key);
        return key;
    }
}
