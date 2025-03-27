package com.example.concertreservation.lock.optimistic;

import com.example.concertreservation.common.enums.UserRole;
import com.example.concertreservation.domain.concert.entity.Concert;
import com.example.concertreservation.domain.concert.entity.ConcertReservationDate;
import com.example.concertreservation.domain.concert.repository.ConcertRepository;
import com.example.concertreservation.domain.concert.repository.ConcertReservationDateRepository;
import com.example.concertreservation.domain.reservation.repository.ReservationRepository;
import com.example.concertreservation.common.lock.optimistic.OptimisticReservationService;
import com.example.concertreservation.domain.user.entity.User;
import com.example.concertreservation.domain.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    public static final int CAPACITY = 100;
    public static final int THREAD_COUNT = 1_000;

    private Concert concert;

    @BeforeEach
    public void setUp() {
        concert = Concert.builder()
                .title("콘서트")
                .capacity(CAPACITY)
                .availableAmount(CAPACITY)
                .build();
        concertRepository.saveAndFlush(concert);

        concertReservationDateRepository.saveAndFlush(
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
        userRepository.saveAllAndFlush(users);
    }

    @AfterEach
    public void tearDown() {
        reservationRepository.deleteAll();
        concertReservationDateRepository.deleteAll();
        concertRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    public void 동시에_콘서트_예매_요청() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

        long startTime = System.currentTimeMillis();
        AtomicInteger userCount = new AtomicInteger(1);
        AtomicInteger optimisticLockExceptionCount = new AtomicInteger(0);
        for (int i = 0; i < THREAD_COUNT; i++) {
            executorService.submit(() -> {
                try {
                    optimisticReservationService.createReservation(concert.getId(), (long) userCount.getAndIncrement());
                } catch (Exception e) {
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

        Concert updatedConcert = concertRepository.findById(concert.getId()).get();
        assertTrue(optimisticLockExceptionCount.get() > 0);
        assertEquals(0, updatedConcert.getAvailableAmount());

        System.out.println("*** 낙관적 락 발생한 예외 수: " + optimisticLockExceptionCount.get());
        System.out.println(CAPACITY + " 예약 가능, " + THREAD_COUNT + "개 요청 처리 시간: " + (endTime - startTime) + "ms");
    }
}
