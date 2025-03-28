package com.example.concertreservation.common.lock.redisson;

import com.example.concertreservation.domain.concert.entity.Concert;
import com.example.concertreservation.domain.concert.repository.ConcertRepository;
import com.example.concertreservation.domain.reservation.entity.Reservation;
import com.example.concertreservation.domain.reservation.repository.ReservationRepository;
import com.example.concertreservation.domain.user.entity.User;
import com.example.concertreservation.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LockReservationProvider {
    private static final Logger log = LoggerFactory.getLogger(LockRedissonManager.class);
    private final LockManager lockManager;
    private final ReservationRepository reservationRepository;
    private final ConcertRepository concertRepository;
    private final UserRepository userRepository;

        public void createReservation(Long concertId, Long userId) {
            Concert concert = concertRepository.findById(concertId)
                    .orElseThrow(() -> new IllegalStateException("해당 콘서트가 존재하지 않습니다."));

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalStateException("해당 사용자가 존재하지 않습니다."));

            String lockKey = "concert:reservation:" + concertId;
            log.debug("락 획득 시도: concertId={}, userId={}", concertId, userId);

            try {
                lockManager.executeWithLock(lockKey, () -> makeReservation(concert, user));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("락 획득 중 인터럽트 발생: " + concertId, e);
            }
        }

        private void makeReservation(Concert concert, User user) {
            long currentReservations = reservationRepository.countByConcertId(concert.getId());
            if (currentReservations >= concert.getCapacity()) {
                throw new IllegalStateException("예약 수량이 초과하였습니다.");
            }

            Reservation reservation = new Reservation();
            reservation.setUser(user);
            reservation.setConcert(concert);
            reservationRepository.save(reservation);
            log.info("예약 완료: concertId={}, userId={}", concert.getId(), user.getId());
        }
    }