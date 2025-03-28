package com.example.concertreservation.domain.concert.service;

import com.example.concertreservation.common.dto.AuthUser;
import com.example.concertreservation.common.exception.InvalidRequestException;
import com.example.concertreservation.domain.concert.dto.request.ConcertSaveRequest;
import com.example.concertreservation.domain.concert.dto.request.ConcertUpdateRequest;
import com.example.concertreservation.domain.concert.dto.response.ConcertDetailResponse;
import com.example.concertreservation.domain.concert.dto.response.ConcertResponse;
import com.example.concertreservation.domain.concert.entity.Concert;
import com.example.concertreservation.domain.concert.entity.ConcertReservationDate;
import com.example.concertreservation.domain.concert.repository.ConcertRepository;
import com.example.concertreservation.domain.concert.repository.ConcertReservationDateRepository;
import com.example.concertreservation.domain.reservation.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.example.concertreservation.common.enums.UserRole.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ConcertServiceTest {

    @Mock
    private ConcertRepository concertRepository;

    @Mock
    private ConcertReservationDateRepository concertReservationDateRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private ZSetOperations<String, String> zSetOperations;

    @InjectMocks
    private ConcertService concertService;

    private Concert concert;

    @BeforeEach
    void setUp() {
        concert = new Concert("테스트 콘서트", "유명 아티스트의 인기있는 콘서트", LocalDateTime.now(), 100, 100,0);
        ReflectionTestUtils.setField(concert, "id", 1L);
    }

    @Test
    void 콘서트를_저장할_수_있다() {
        // given
        ConcertSaveRequest saveRequest = new ConcertSaveRequest("테스트 콘서트", "유명 아티스트의 인기있는 콘서트", LocalDateTime.now(), 100, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));
        ConcertReservationDate reservationDate = new ConcertReservationDate(concert, saveRequest.getStartDate(), saveRequest.getEndDate());

        given(concertRepository.save(any(Concert.class))).willReturn(concert);
        given(concertReservationDateRepository.save(any(ConcertReservationDate.class))).willReturn(reservationDate);

        // when
        ConcertResponse response = concertService.saveConcert(saveRequest);

        // then
        assertThat(response.getTitle()).isEqualTo("테스트 콘서트");
        assertThat(response.getDescription()).isEqualTo("유명 아티스트의 인기있는 콘서트");
    }

    @Test
    void 존재하지_않는_콘서트를_삭제하면_InvalidRequestException을_던진다() {
        // given
        Long concertId = 99L;
        given(concertRepository.findById(concertId)).willReturn(Optional.empty());

        // when & then
        assertThrows(InvalidRequestException.class, () -> concertService.deleteConcert(concertId));
    }

    @Test
    void 콘서트를_수정할_수_있다() {
        // given
        ConcertUpdateRequest updateRequest = new ConcertUpdateRequest("콘서트 이름 업데이트", "콘서트 소개 업데이트", LocalDateTime.now().plusDays(10), 150, LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(3));
        ConcertReservationDate reservationDate = new ConcertReservationDate(concert, updateRequest.getStartDate(), updateRequest.getEndDate());
        given(concertReservationDateRepository.findByConcertIdWithConcert(concert.getId())).willReturn(Optional.of(reservationDate));

        // when
        ConcertResponse response = concertService.updateConcert(concert.getId(), updateRequest);

        // then
        assertThat(response.getTitle()).isEqualTo("콘서트 이름 업데이트");
        assertThat(response.getDescription()).isEqualTo("콘서트 소개 업데이트");
    }

    @Test
    void 콘서트를_조회할_수_있다() {
        // given
        Long concertId = 1L;
        ConcertReservationDate reservationDate = new ConcertReservationDate(concert, LocalDateTime.now(), LocalDateTime.now().plusDays(1));

        // redisTemplate 세팅
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.increment(anyString())).willReturn(1L);
        given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
        given(zSetOperations.incrementScore(anyString(), anyString(), anyDouble())).willReturn(1.0);

        // 조회
        given(concertReservationDateRepository.findByConcertIdWithConcert(concertId)).willReturn(Optional.of(reservationDate));

        // when
        ConcertDetailResponse response = concertService.getConcert(concertId, new AuthUser(1L, "testuser@email.com", ROLE_USER));

        // then
        assertThat(response.getTitle()).isEqualTo(concert.getTitle());
        assertThat(response.getDescription()).isEqualTo(concert.getDescription());
    }
}