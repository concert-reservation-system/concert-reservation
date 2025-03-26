package com.example.concertreservation.domain.reservation;

import com.example.concertreservation.common.enums.UserRole;
import com.example.concertreservation.domain.concert.entity.Concert;
import com.example.concertreservation.domain.concert.repository.ConcertRepository;
import com.example.concertreservation.domain.reservation.facade.LettuceLockReservationFacade;
import com.example.concertreservation.domain.reservation.repository.ReservationRepository;
import com.example.concertreservation.domain.user.entity.User;
import com.example.concertreservation.domain.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class LettuceLockReservationTest {

    @Autowired
    private LettuceLockReservationFacade lettuceLockReservationFacade;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ConcertRepository concertRepository;
    @Autowired
    private UserRepository userRepository;

    public static int CAPACITY = 100;

    private Concert concert;
    private User user;

    @BeforeEach
    public void setUp() {
        concert = Concert.builder().title("콘서트").capacity(CAPACITY).availableAmount(CAPACITY).build();
        concertRepository.saveAndFlush(concert);
        user = new User("test", "1234", UserRole.ROLE_USER);
        userRepository.saveAndFlush(user);
    }

    @AfterEach
    public void tearDown() {
        reservationRepository.deleteAll();
        concertRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    public void 동시에_콘서트_예매_요청() throws InterruptedException {
        int threadCount = 1_000;
        ExecutorService executorService = Executors.newFixedThreadPool(100);
        CountDownLatch latch = new CountDownLatch(threadCount);

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    lettuceLockReservationFacade.create(concert.getId(), user.getId());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        long endTime = System.currentTimeMillis();
        System.out.println(CAPACITY + " 예약 가능, " + threadCount + "개 요청 처리 시간: " + (endTime - startTime) + "ms");

        Concert updatedConcert = concertRepository.findById(concert.getId()).get();
        assertEquals(0, updatedConcert.getAvailableAmount());
    }

}
