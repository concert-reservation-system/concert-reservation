package com.example.concertreservation.common.fairlock;

import com.example.concertreservation.domain.concert.entity.Concert;
import com.example.concertreservation.domain.concert.repository.ConcertRepository;
import com.example.concertreservation.domain.reservation.repository.ReservationRepository;
import com.example.concertreservation.domain.user.entity.User;
import com.example.concertreservation.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class LockReservationServiceConcurrencyTest {

    @Autowired
    private LockReservationService lockReservationService;

    @Autowired
    private ConcertRepository concertRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    private Long concertId;

    @BeforeEach
    void setUp() {
        reservationRepository.deleteAll();
        userRepository.deleteAll();
        concertRepository.deleteAll();

        Concert concert = Concert.builder()
                .title("동시성 테스트 콘서트")
                .capacity(1)
                .availableAmount(1)
                .build();

        concertId = concertRepository.save(concert).getId();

        for (int i = 1; i <= 10; i++) {
            userRepository.save(User.builder().email("user" + i + "@test.com").build());
        }
    }

    @Test
    @DisplayName("공정 락_동시에 여러 유저가 예약 시도하면 오직 1명만 성공한다")
    void fairLock_동시성_예약_테스트() throws InterruptedException {
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        userRepository.findAll().forEach(user -> {
            executorService.submit(() -> {
                try {
                    lockReservationService.executeWithLock(concertId, user.getId());
                } catch (Exception e) {
                } finally {
                    latch.countDown();
                }
            });
        });

        latch.await();
        Thread.sleep(200);

        // 예약은 1건만 성공해야 함!!!!
        long successCount = reservationRepository.countByConcertId(concertId);
        assertThat(successCount).isEqualTo(1);
    }
}