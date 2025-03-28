package com.example.concertreservation.lock.redisson;

import com.example.concertreservation.common.lock.redisson.LockRedissonManager;
import com.example.concertreservation.common.lock.redisson.LockReservationService;
import com.example.concertreservation.domain.concert.entity.Concert;
import com.example.concertreservation.domain.concert.entity.ConcertReservationDate;
import com.example.concertreservation.domain.concert.repository.ConcertRepository;
import com.example.concertreservation.domain.reservation.repository.ReservationRepository;
import com.example.concertreservation.domain.concert.repository.ConcertReservationDateRepository;
import com.example.concertreservation.domain.user.entity.User;
import com.example.concertreservation.domain.user.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("dev")
public class LockReservationServiceFailTest {

    private static final Logger log = LoggerFactory.getLogger(LockRedissonManager.class);

    @Autowired
    private LockReservationService lockReservationService;

    @Autowired
    private ConcertRepository concertRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ConcertReservationDateRepository concertReservationDateRepository;

    private Long concertId;

    @BeforeEach
    public void setUp() {
        Concert concert = new Concert();
        concert.setTitle("지디 콘서트");
        concert.setCapacity(1);
        concert.setAvailableAmount(1);
        concertRepository.save(concert);
        concertId = concert.getId();

        ConcertReservationDate reservationDate = new ConcertReservationDate();
        reservationDate.setConcert(concert);
        reservationDate.setStartDate(LocalDateTime.now().minusMinutes(10));
        reservationDate.setEndDate(LocalDateTime.now().plusMinutes(10));
        concertReservationDateRepository.save(reservationDate);

        for (int i = 1; i <= 100; i++) {
            userRepository.save(new User("user" + i + "@test.com"));
        }
    }

    @Test
    @DisplayName("공정 락_동시에 여러 유저가 예약 시도하면 오직 1명만 성공한다")
    public void fairLock_동시성_예약_테스트() throws InterruptedException {
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        userRepository.findAll().forEach(user -> {
            executorService.submit(() -> {
                try {
                    lockReservationService.executeWithLock(concertId, user.getId());
                } catch (Exception e) {
                    log.warn("락 충돌: {}", e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        });

        latch.await();
        executorService.shutdown();

        // 예약은 1건만 성공해야 함!!!!
        long reservationSuccessCount = reservationRepository.countByConcertId(concertId);
        assertThat(reservationSuccessCount).isEqualTo(3);
    }
}
