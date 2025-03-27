package com.example.concertreservation.domain.concert.scheduler;

import com.example.concertreservation.domain.concert.repository.ConcertRepository;
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

    private final RedisTemplate<String, String> redisTemplate;
    private final ConcertRepository concertRepository;

    @CacheEvict(value = "concertList", allEntries = true)
    @Transactional
    @Scheduled(fixedRate = 300_000) // 5분에 한번
    public void flushViewCountToDB() {
        Set<String> keys = redisTemplate.keys("concert:view:*");

        if (keys.isEmpty()) {
            log.info("조회수 반영할 Redis 키 없음");
            return;
        }

        for (String key : keys) {
            try {
                Long concertId = Long.parseLong(key.replace("concert:view:", ""));
                String value = redisTemplate.opsForValue().get(key);

                if (value == null) continue;

                int redisViewCount  = Integer.parseInt(value);
                int currentViewCount = concertRepository.findViewCountById(concertId);
                int totalViewCount = currentViewCount + redisViewCount;
                concertRepository.updateViewCount(concertId, totalViewCount);

                redisTemplate.delete(key);
                log.info("조회수 DB 반영 완료 - concertId: {}, viewCount: {}", concertId, totalViewCount);
            } catch (Exception e) {
                log.error("조회수 DB 반영 실패 - key: {}", key, e);
            }
        }
    }
}
