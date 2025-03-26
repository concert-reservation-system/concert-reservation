package com.example.concertreservation.domain.reservation.service;

import com.example.concertreservation.domain.concert.entity.Concert;
import com.example.concertreservation.domain.concert.repository.ConcertRepository;
import com.example.concertreservation.domain.reservation.entity.Reservation;
import com.example.concertreservation.domain.reservation.repository.ReservationRepository;
import com.example.concertreservation.domain.user.entity.User;
import com.example.concertreservation.domain.user.repository.UserRepository;
import com.example.concertreservation.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final ConcertRepository concertRepository;

    @Transactional
    public void createReservation(Long concertId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        Concert concert = concertRepository.findById(concertId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "콘서트를 찾을 수 없습니다."));

        if (reservationRepository.findByUserIdAndConcertId(userId, concertId).isPresent()) {
            throw new IllegalStateException("이미 이 콘서트를 예약했습니다.");
        }

        Reservation reservation = new Reservation(user, concert);
        reservationRepository.save(reservation);
        concert.decreaseAvailableAmount();
        System.out.println("예약 성공: userId=" + userId);
    }
}
