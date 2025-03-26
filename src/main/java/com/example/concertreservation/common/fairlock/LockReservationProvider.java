//package com.example.concertreservation.common.fairlock;
//
//import com.example.concertreservation.domain.concert.entity.Concert;
//import com.example.concertreservation.domain.concert.repository.ConcertRepository;
//import com.example.concertreservation.domain.reservation.entity.Reservation;
//import com.example.concertreservation.domain.reservation.repository.ReservationRepository;
//import com.example.concertreservation.domain.user.entity.User;
//import com.example.concertreservation.domain.user.repository.UserRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//@Service
//@RequiredArgsConstructor
//public class LockReservationProvider {
//
//    private final LockManager lockManager;  // LockManager 인터페이스 사용
//    private final ReservationRepository reservationRepository;
//    private final ConcertRepository concertRepository;
//    private final UserRepository userRepository;
//
//    public void createReservation(Long concertId, Long userId) {
//        // 예약 시 콘서트와 유저에 대한 정보를 락을 걸고 처리하도록
//        String lockKey = "concert:reservation:" + concertId;
//
//        try {
//            lockManager.executeWithLock(lockKey, () -> processReservation(concertId, userId));
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//            throw new IllegalStateException("락 획득 중 인터럽트 발생: " + concertId, e);
//        }
//    }
//
//    private void processReservation(Long concertId, Long userId) {
//        // 예약할 콘서트 정보 가져오기
//        Concert concert = concertRepository.findById(concertId)
//                .orElseThrow(() -> new IllegalStateException("해당 콘서트가 존재하지 않습니다."));
//
//        // 예약할 유저 정보 가져오기
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new IllegalStateException("해당 사용자가 존재하지 않습니다."));
//
//        // 현재 예약 가능한 수량 체크
//        long currentReservations = reservationRepository.countByConcertId(concertId);
//
//        // 예약 수량이 최대 수량을 초과할 경우 예외 발생
//        if (currentReservations >= concert.getCapacity()) {
//            throw new IllegalStateException("예약 수량이 초과하였습니다.");
//        }
//
//        // 예약 처리
//        Reservation reservation = new Reservation();
//        reservation.setUser(user);
//        reservation.setConcert(concert);
//        reservationRepository.save(reservation);
//    }
//}