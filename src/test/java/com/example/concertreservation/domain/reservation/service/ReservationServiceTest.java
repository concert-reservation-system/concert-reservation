package com.example.concertreservation.domain.reservation.service;

import com.example.concertreservation.common.dto.AuthUser;
import com.example.concertreservation.common.enums.UserRole;
import com.example.concertreservation.common.exception.InvalidRequestException;
import com.example.concertreservation.common.exception.NotFoundException;
import com.example.concertreservation.domain.concert.entity.Concert;
import com.example.concertreservation.domain.concert.entity.ConcertReservationDate;
import com.example.concertreservation.domain.concert.repository.ConcertRepository;
import com.example.concertreservation.domain.concert.repository.ConcertReservationDateRepository;
import com.example.concertreservation.domain.reservation.dto.response.ReservationResponse;
import com.example.concertreservation.domain.reservation.dto.response.ReservationSearchResponse;
import com.example.concertreservation.domain.reservation.entity.Reservation;
import com.example.concertreservation.domain.reservation.repository.ReservationRepository;
import com.example.concertreservation.domain.user.entity.User;
import com.example.concertreservation.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ConcertRepository concertRepository;
    @Mock
    private ConcertReservationDateRepository concertReservationDateRepository;
    @InjectMocks
    private ReservationService reservationService;

    private Long userId = 1L;
    private Long concertId = 1L;

    AuthUser authUser = new AuthUser(userId, "a@a.com", UserRole.ROLE_USER);
    User user = new User("a@a.com", "1111", UserRole.ROLE_USER);
    Concert concert = new Concert("title",
            "description",
            LocalDateTime.of(2025, 04, 30, 14, 0, 0),
            10,
            10,
            0);
    ConcertReservationDate concertReservationDate = new ConcertReservationDate(concert,
            LocalDateTime.now().minusMinutes(10),
            LocalDateTime.now().plusMinutes(10));


    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(user, "id", userId);
        ReflectionTestUtils.setField(concert, "id", concertId);
        ReflectionTestUtils.setField(concertReservationDate, "concert", concert);
    }

    @Test
    @DisplayName("콘서트 예약 시 유저가 존재하지 않는 경우 예외 발생")
    void reservation_create_user_not_found_fail() {
        // given
        given(userRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () ->
                        reservationService.createReservation(concertId, userId),
                "해당 유저가 존재하지 않습니다."
        );
    }

    @Test
    @DisplayName("콘서트 예약 시 콘서트가 존재하지 않는 경우 예외 발생")
    void reservation_create_concert_not_found_fail() {
        // given
        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
        given(concertRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () ->
                        reservationService.createReservation(concertId, userId),
                "해당 콘서트가 존재하지 않습니다."
        );
    }

    @Test
    @DisplayName("콘서트 예약 시 콘서트 예매 일정이 존재하지 않는 경우 예외 발생")
    void reservation_create_concert_reservation_date_not_found_fail() {
        // given
        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
        given(concertRepository.findById(anyLong())).willReturn(Optional.of(concert));
        given(concertReservationDateRepository.findByConcertId(anyLong())).willReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () ->
                        reservationService.createReservation(concertId, userId),
                "해당 콘서트의 예매 일정이 존재하지 않습니다."
        );
    }

    @Test
    @DisplayName("콘서트 예약 시 예약 기간이 아닌 경우 예외 발생")
    void reservation_create_not_in_reservation_date_fail() {
        // given
        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
        given(concertRepository.findById(anyLong())).willReturn(Optional.of(concert));
        given(concertReservationDateRepository.findByConcertId(anyLong())).willReturn(Optional.of(concertReservationDate));
        ReflectionTestUtils.setField(concertReservationDate, "startDate", LocalDateTime.now().plusMinutes(10));

        // when & then
        assertThrows(InvalidRequestException.class, () ->
                        reservationService.createReservation(concertId, userId),
                "콘서트 예약 기간이 아닙니다."
        );
    }

    @Test
    @DisplayName("콘서트 예약 시 잔여 좌석이 존재하지 않는 경우 예외 발생")
    void reservation_create_no_available_amount_fail() {
        // given
        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
        given(concertRepository.findById(anyLong())).willReturn(Optional.of(concert));
        given(concertReservationDateRepository.findByConcertId(anyLong())).willReturn(Optional.of(concertReservationDate));
        ReflectionTestUtils.setField(concert, "availableAmount", 0);

        // when & then
        assertThrows(InvalidRequestException.class, () ->
                        reservationService.createReservation(concertId, userId),
                "잔여 좌석이 없습니다."
        );
    }

    @Test
    @DisplayName("콘서트 예약 시 이미 예약한 콘서트일 경우 예외 발생")
    void reservation_create_already_make_reservation_fail() {
        // given
        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
        given(concertRepository.findById(anyLong())).willReturn(Optional.of(concert));
        given(concertReservationDateRepository.findByConcertId(anyLong())).willReturn(Optional.of(concertReservationDate));
        Reservation reservation = new Reservation(user, concert);
        given(reservationRepository.findByUserIdAndConcertId(anyLong(), anyLong())).willReturn(Optional.of(reservation));

        // when & then
        assertThrows(InvalidRequestException.class, () ->
                        reservationService.createReservation(concertId, userId),
                "이미 예약한 콘서트입니다."
        );
    }

    @Test
    @DisplayName("콘서트 예약 성공")
    void reservation_create_success() {
        // given
        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
        given(concertRepository.findById(anyLong())).willReturn(Optional.of(concert));
        given(concertReservationDateRepository.findByConcertId(anyLong())).willReturn(Optional.of(concertReservationDate));
        given(reservationRepository.findByUserIdAndConcertId(anyLong(), anyLong())).willReturn(Optional.empty());

        // when
        ReservationResponse reservation = reservationService.createReservation(concertId, userId);

        // then
        assertEquals(reservation.getConcertId(), concert.getId());
        assertEquals(reservation.getUserId(), userId);
    }

    @Test
    @DisplayName("콘서트 내 예약 조회")
    void reservation_find_reservations() {
        // given
        Reservation reservation = new Reservation(user, concert);
        reservationRepository.save(reservation);
        ReservationSearchResponse response = ReservationSearchResponse.from(reservation);
        Page<ReservationSearchResponse> reservations = new PageImpl<>(List.of(response));

        given(reservationRepository.findMyReservations(any(Pageable.class),
                anyLong(),
                anyString(),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).willReturn(reservations);

        // when
        Page<ReservationSearchResponse> result = reservationService.findMyReservations(authUser,
                1,
                10,
                "title",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        // then
        assertNotNull(result);
    }
}

