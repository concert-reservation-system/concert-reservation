package com.example.concertreservation.lock.blockingqueue;

import com.example.concertreservation.common.enums.UserRole;
import com.example.concertreservation.common.lock.reentrant.ReentrantReservationService;
import com.example.concertreservation.domain.concert.entity.Concert;
import com.example.concertreservation.domain.concert.entity.ConcertReservationDate;
import com.example.concertreservation.domain.concert.repository.ConcertRepository;
import com.example.concertreservation.domain.concert.repository.ConcertReservationDateRepository;
import com.example.concertreservation.domain.reservation.repository.ReservationRepository;
import com.example.concertreservation.domain.user.entity.User;
import com.example.concertreservation.domain.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@SpringBootTest
public class ReservationQueueServiceTest {

    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ConcertRepository concertRepository;
    @Autowired
    private ConcertReservationDateRepository concertReservationDateRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ReentrantReservationService reentrantReservationService;

    public static final int CAPACITY = 10;
    public static final int THREAD_COUNT = 100;

    private Concert concert;
    private int userId = 1;

    @BeforeEach
    public void setUp() {
        concert = Concert.builder()
                .title("콘서트")
                .capacity(CAPACITY)
                .availableAmount(CAPACITY)
                .build();
        concertRepository.save(concert);

        concertReservationDateRepository.save(
                ConcertReservationDate.builder()
                        .concert(concert)
                        .startDate(LocalDateTime.of(0000, 1, 1, 0, 0))
                        .endDate(LocalDateTime.of(9999, 12, 31, 23, 59))
                        .build()
        );

        List<User> users = new ArrayList<>();
        for (int i = 0; i < THREAD_COUNT; i++) {
            users.add(User.builder()
                    .email("user" + i + "@gmail.com")
                    .password("1234")
                    .userRole(UserRole.ROLE_USER)
                    .build());
        }
        userRepository.saveAll(users);
        userId = Math.toIntExact(users.get(0).getId());
    }

    @AfterEach
    public void tearDown() {
        reservationRepository.deleteAll();
        concertReservationDateRepository.deleteAll();
        concertRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Reentrant lock 콘서트 예매 성공")
    public void reentrant_reservation_success() throws InterruptedException {
        // 대기열(BlockingQueue)을 사용하여 요청 처리
        ExecutorService executorService = new ThreadPoolExecutor(
                10, // 코어 스레드 개수
                10, // 최대 스레드 개수
                60L, // 유휴 스레드 유지 시간
                TimeUnit.SECONDS, // 시간 단위
                new LinkedBlockingQueue<>()); // 작업 대기열

        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

        long startTime = System.currentTimeMillis();
        AtomicInteger userCount = new AtomicInteger(userId);
        for (int i = 0; i < THREAD_COUNT; i++) {
            executorService.submit(() -> {
                try {
                    reentrantReservationService.createReservation(concert.getId(), (long) userCount.getAndIncrement());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executorService.shutdown();

        long endTime = System.currentTimeMillis();

        Concert updatedConcert = concertRepository.findById(concert.getId()).get();
        assertEquals(0, updatedConcert.getAvailableAmount());
        log.info(CAPACITY + " 예약 가능, " + THREAD_COUNT + "개 요청 처리 시간: {}ms", endTime - startTime);
    }

    @Test
    @DisplayName("Reentrant lock 콘서트 잔여 좌석 초과")
    public void reentrant_reservation_fail() throws InterruptedException {
        int threadCount = CAPACITY + 1;

        // 대기열(BlockingQueue)을 사용하여 요청 처리
        ExecutorService executorService = new ThreadPoolExecutor(
                10, // 코어 스레드 개수
                10, // 최대 스레드 개수
                60L, // 유휴 스레드 유지 시간
                TimeUnit.SECONDS, // 시간 단위
                new LinkedBlockingQueue<>()); // 작업 대기열

        CountDownLatch latch = new CountDownLatch(threadCount);
        List<Exception> exceptions = Collections.synchronizedList(new ArrayList<>());

        AtomicInteger userCount = new AtomicInteger(userId);
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    reentrantReservationService.createReservation(concert.getId(), (long) userCount.getAndIncrement());
                } catch (Exception e) {
                    exceptions.add(e);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executorService.shutdown();

        if (!exceptions.isEmpty()) {
            assertEquals("잔여 좌석이 없습니다.", exceptions.get(0).getMessage());
        }
    }
}
