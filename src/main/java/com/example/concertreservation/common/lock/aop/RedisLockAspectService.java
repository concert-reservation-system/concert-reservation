package com.example.concertreservation.common.lock.aop;

import com.example.concertreservation.common.exception.InvalidRequestException;
import com.example.concertreservation.common.exception.NotFoundException;
import com.example.concertreservation.common.lock.aop.annotation.RedisLock;
import com.example.concertreservation.domain.concert.entity.Concert;
import com.example.concertreservation.domain.concert.entity.ConcertReservationDate;
import com.example.concertreservation.domain.concert.repository.ConcertRepository;
import com.example.concertreservation.domain.concert.repository.ConcertReservationDateRepository;
import com.example.concertreservation.domain.reservation.entity.Reservation;
import com.example.concertreservation.domain.reservation.repository.ReservationRepository;
import com.example.concertreservation.domain.user.entity.User;
import com.example.concertreservation.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RedisLockAspectService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final ConcertRepository concertRepository;
    private final ConcertReservationDateRepository concertReservationDateRepository;

    @RedisLock(key = "#concertId")
    public void createAopReservation(Long concertId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("해당 유저가 존재하지 않습니다." + concertId));

        Concert concert = concertRepository.findById(concertId)
                .orElseThrow(() -> new NotFoundException("해당 콘서트가 존재하지 않습니다."));

        ConcertReservationDate reservationDate = concertReservationDateRepository
                .findByConcertId(concert.getId())
                .orElseThrow(() -> new NotFoundException("해당 콘서트의 예매 일정이 존재하지 않습니다."));

        if (reservationDate.getStartDate().isAfter(LocalDateTime.now()) ||
                reservationDate.getEndDate().isBefore(LocalDateTime.now())) {
            throw new InvalidRequestException("콘서트 예약 기간이 아닙니다.");
        }

        if (concert.getAvailableAmount() == 0) {
            throw new InvalidRequestException("잔여 좌석이 없습니다.");
        }

        reservationRepository.findByUserIdAndConcertId(userId, concertId)
                .ifPresent(reservation -> {
                    throw new InvalidRequestException("이미 예약한 콘서트입니다.");
                });

        reservationRepository.save(new Reservation(user, concert));
        concert.decreaseAvailableAmount();
    }
}
