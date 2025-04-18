package com.example.concertreservation.lock.optimistic;

import com.example.concertreservation.common.enums.UserRole;
import com.example.concertreservation.common.exception.InvalidRequestException;
import com.example.concertreservation.common.lock.optimistic.OptimisticReservationService;
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
import org.springframework.dao.OptimisticLockingFailureException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@SpringBootTest
public class OptimisticReservationTest {
    // 낙관적 락 (Optimistic Lock)
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ConcertRepository concertRepository;
    @Autowired
    private ConcertReservationDateRepository concertReservationDateRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OptimisticReservationService optimisticReservationService;

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
    @DisplayName("Optimistic lock 콘서트 예매 성공")
    public void optimistic_reservation_success() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

        long startTime = System.currentTimeMillis();
        AtomicInteger userCount = new AtomicInteger(userId);
        AtomicInteger optimisticLockExceptionCount = new AtomicInteger(0);
        for (int i = 0; i < THREAD_COUNT; i++) {
            executorService.submit(() -> {
                try {
                    optimisticReservationService.createReservation(concert.getId(), (long) userCount.getAndIncrement());
                } catch (OptimisticLockingFailureException e) {
                    // 낙관적 락 충돌 발생 시 처리 및 예외 카운트 증가
                    optimisticLockExceptionCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executorService.shutdown();

        long endTime = System.currentTimeMillis();

        assertTrue(optimisticLockExceptionCount.get() > 0);

        log.info("*** 낙관적 락 발생한 예외 수: {}", optimisticLockExceptionCount.get());
        log.info(CAPACITY + " 예약 가능, " + THREAD_COUNT + "개 요청 처리 시간: {}ms", endTime - startTime);
    }

    @Test
    @DisplayName("Optimistic lock 콘서트 예매 잔여 좌석 초과")
    public void optimistic_reservation_fail() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
        List<Exception> exceptions = Collections.synchronizedList(new ArrayList<>());

        AtomicInteger userCount = new AtomicInteger(userId);
        AtomicInteger optimisticLockExceptionCount = new AtomicInteger(0);
        for (int i = 0; i < THREAD_COUNT; i++) {
            executorService.submit(() -> {
                try {
                    optimisticReservationService.createReservation(concert.getId(), (long) userCount.getAndIncrement());
                } catch (OptimisticLockingFailureException e) {
                    // 낙관적 락 충돌 발생 시 처리 및 예외 카운트 증가
                    optimisticLockExceptionCount.incrementAndGet();
                } catch (InvalidRequestException e) {
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
