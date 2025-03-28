package com.example.concertreservation.domain.concert.scheduler;

import com.example.concertreservation.domain.concert.repository.ConcertRepository;
import com.example.concertreservation.domain.concert.service.ConcertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Component
public class ViewCountSyncScheduler {

    private final ConcertService concertService;

    @Scheduled(cron = "0 0 0 * * *") // 매일 자정
    public void runFlushViewCountJob() {
        log.info("조회수 DB 반영 작업 시작");
        concertService.flushViewCountToDB();
    }
}
