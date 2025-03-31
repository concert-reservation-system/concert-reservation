package com.example.concertreservation.lock.aop;

import com.example.concertreservation.common.enums.UserRole;
import com.example.concertreservation.common.lock.aop.RedisLockAspectService;
import com.example.concertreservation.domain.concert.entity.Concert;
import com.example.concertreservation.domain.concert.entity.ConcertReservationDate;
import com.example.concertreservation.domain.concert.repository.ConcertRepository;
import com.example.concertreservation.domain.concert.repository.ConcertReservationDateRepository;
import com.example.concertreservation.domain.reservation.repository.ReservationRepository;
import com.example.concertreservation.domain.user.entity.User;
import com.example.concertreservation.domain.user.repository.UserRepository;
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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ReservationAopServiceTest {

    @Autowired
    private RedisLockAspectService reservationService;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ConcertRepository concertRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ConcertReservationDateRepository concertReservationDateRepository;

    public static int CAPACITY = 100;
    public static final int THREAD_COUNT = 101;

    private Concert concert;
    private User user;

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
    @DisplayName("동시에 콘서트 최대 정원 보다 많은 수의 유저가 예약 시도하고 일부만 성공한다.")
    public void aopLock_동시성_예약_테스트_실패() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(100);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
        List<Exception> exceptions = Collections.synchronizedList(new ArrayList<>());

        AtomicInteger count = new AtomicInteger(1);
        for (int i = 0; i < THREAD_COUNT; i++) {
            executorService.submit(() -> {
                try {
                    reservationService.createAopReservation(concert.getId(), (long) count.getAndIncrement());
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

    @Test
    @DisplayName("동시에 콘서트 최대 정원 수의 유저가 예약 시도하고 모두가 성공한다.")
    public void aopLock_동시성_예약_테스트_성공() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(100);
        CountDownLatch latch = new CountDownLatch(100);

        AtomicInteger count = new AtomicInteger(1);
        for (int i = 0; i < 100; i++) {
            executorService.submit(() -> {
                try {
                    reservationService.createAopReservation(concert.getId(), (long) count.getAndIncrement());
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executorService.shutdown();

        Concert updatedConcert = concertRepository.findById(concert.getId()).get();
        assertEquals(0, updatedConcert.getAvailableAmount(), "모든 좌석 예매 완료");
    }
}