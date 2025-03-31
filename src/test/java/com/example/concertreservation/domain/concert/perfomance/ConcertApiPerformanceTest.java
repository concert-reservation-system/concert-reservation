package com.example.concertreservation.domain.concert.perfomance;

import com.example.concertreservation.common.enums.UserRole;
import com.example.concertreservation.config.WithMockAuthUser;
import com.example.concertreservation.domain.concert.repository.ConcertRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.ArgumentMatchers.*;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest
@AutoConfigureMockMvc
class ConcertApiPerformanceTest {

    @Autowired
    MockMvc mockMvc;

    @SpyBean
    ConcertRepository concertRepository;

    @Test
    @DisplayName("콘서트 목록 조회 캐시 성능 테스트")
    @WithMockAuthUser(userId = 1L, email = "ex@example.com", role = UserRole.ROLE_USER)
    void 콘서트_목록_조회_캐시_성능_테스트() throws Exception {
        int iterations = 500;
        long totalDuration = 0L;

        for (int i = 0; i < iterations; i++) {
            Instant start = Instant.now();
            mockMvc.perform(get("/api/concerts")
                    .param("page", "1")
                    .param("size", "10")
                    .param("keyword", "콘서트")
                    .param("fromDate", "2025-01-01T00:00:00")
                    .param("toDate", "2025-12-31T23:59:59")
            ).andExpect(status().isOk());
            Instant end = Instant.now();
            totalDuration += Duration.between(start, end).toMillis();
        }

        System.out.println("평균 응답 시간: " + (totalDuration / (double) iterations) + "ms");
    }

    @Test
    @DisplayName("다중 스레드 목록 조회 시 캐싱 효과 검증")
    void 캐시_적중_시_동시_요청에서도_DB_1회_조회() throws InterruptedException {
        int threadCount = 50; // 병렬 스레드 수
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    mockMvc.perform(get("/api/concerts")
                                    .param("page", "1")
                                    .param("size", "10")
                                    .param("keyword", "콘서트")
                                    .param("fromDate", "2025-01-01T00:00:00")
                                    .param("toDate", "2025-12-31T23:59:59"))
                            .andExpect(status().isOk());
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(); // 모든 스레드 완료까지 대기

        // DB 조회 횟수 확인
        verify(concertRepository, atMost(1))
                .searchConcerts(
                        any(Pageable.class),
                        anyString(),
                        any(LocalDateTime.class),
                        any(LocalDateTime.class)
                );
    }
}