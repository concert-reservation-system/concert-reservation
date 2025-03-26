package com.example.concertreservation.common.fairlock;

import com.example.concertreservation.domain.concert.entity.Concert;
import com.example.concertreservation.domain.concert.repository.ConcertRepository;
import com.example.concertreservation.domain.reservation.entity.Reservation;
import com.example.concertreservation.domain.reservation.repository.ReservationRepository;
import com.example.concertreservation.domain.user.entity.User;
import com.example.concertreservation.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LockReservationProvider {

    private final LockManager lockManager;
    private final ReservationRepository reservationRepository;
    private final ConcertRepository concertRepository;
    private final UserRepository userRepository;

    public void createReservation(Long concertId, Long userId) {
        String lockKey = "concert:reservation:" + concertId;
        System.out.println("락 획득 시도: concertId=" + concertId + ", userId=" + userId);

        try {
            lockManager.executeWithLock(lockKey, () -> processReservation(concertId, userId));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("락 획득 중 인터럽트 발생: " + concertId + ", userId=" + userId);
            throw new IllegalStateException("락 획득 중 인터럽트 발생: " + concertId, e);
        }
    }

    private void processReservation(Long concertId, Long userId) {
        Concert concert = concertRepository.findById(concertId)
                .orElseThrow(() -> {
                    System.out.println("해당 콘서트가 존재하지 않습니다: concertId=" + concertId);
                    return new IllegalStateException("해당 콘서트가 존재하지 않습니다.");
                });
        System.out.println("콘서트 정보 확인: concertId=" + concertId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    System.out.println("해당 사용자가 존재하지 않습니다: userId=" + userId);
                    return new IllegalStateException("해당 사용자가 존재하지 않습니다.");
                });
        System.out.println("유저 정보 확인: userId=" + userId);

        long currentReservations = reservationRepository.countByConcertId(concertId);
        System.out.println("현재 예약 수량: " + currentReservations + ", 콘서트 용량: " + concert.getCapacity());

        if (currentReservations >= concert.getCapacity()) {
            System.out.println("예약 수량 초과: concertId=" + concertId);
            throw new IllegalStateException("예약 수량이 초과하였습니다.");
        }

        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setConcert(concert);
        reservationRepository.save(reservation);
        System.out.println("예약 완료: concertId=" + concertId + ", userId=" + userId);
    }
}