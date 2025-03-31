package com.example.concertreservation.domain.reservation.controller;

import com.example.concertreservation.common.enums.UserRole;
import com.example.concertreservation.config.WithMockAuthUser;
import com.example.concertreservation.domain.concert.entity.Concert;
import com.example.concertreservation.domain.concert.entity.ConcertReservationDate;
import com.example.concertreservation.domain.concert.repository.ConcertRepository;
import com.example.concertreservation.domain.concert.repository.ConcertReservationDateRepository;
import com.example.concertreservation.domain.user.entity.User;
import com.example.concertreservation.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ConcertRepository concertRepository;

    @Autowired
    private ConcertReservationDateRepository concertReservationDateRepository;

    @Autowired
    private UserRepository userRepository;

    private Concert concert;
    private User user;

    @BeforeEach
    public void setUp() {
//        userRepository.deleteAll();
//        concertRepository.deleteAll();
//        concertReservationDateRepository.deleteAll();

        concert = concertRepository.saveAndFlush(Concert.builder()
                .title("테스트 콘서트")
                .capacity(2) // 예약 가능한 좌석 2개
                .availableAmount(2)
                .build());

        concertReservationDateRepository.saveAndFlush(
                ConcertReservationDate.builder()
                        .concert(concert)
                        .startDate(LocalDateTime.of(2025, 3, 1, 0, 0))
                        .endDate(LocalDateTime.of(2025, 3, 31, 23, 59))
                        .build()
        );

        user = userRepository.saveAndFlush(User.builder()
                .email("mytestuser@example.com")
                .password("1234")
                .userRole(UserRole.ROLE_USER)
                .build());
    }

//    @AfterEach
//    public void tearDown() {
//        concertRepository.deleteAll();
//        concertReservationDateRepository.deleteAll();
//        userRepository.deleteAll();
//    }

    @Test
    @WithMockAuthUser(userId = 89L, email = "mytestuser@example.com", role = UserRole.ROLE_USER) // 인증된 사용자로 요청
    public void 콘서트_예약_성공() throws Exception {
        // 예약 요청 성공 테스트
        mockMvc.perform(post("/api/reservations/{concertId}", concert.getId()))
                .andExpect(status().isCreated());
    }

    @Test
    public void 로그인되지않은_사용자_예약_요청() throws Exception {
        // 인증되지 않은 경우
        mockMvc.perform(post("/api/reservations/{concertId}", concert.getId()))
                .andExpect(status().isForbidden())
                .andExpect(content().string("로그인이 필요합니다."));
    }

    @Test
    @WithMockAuthUser(userId = 89L, email = "mytestuser@example.com", role = UserRole.ROLE_USER) // 인증된 사용자로 요청
    public void 존재하지않는_콘서트_예약_요청() throws Exception {
        // 존재하지 않는 콘서트에 예약 시도
        Long fakeConcertId = 7777L;

        mockMvc.perform(post("/api/reservations/{concertId}", fakeConcertId))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("해당 콘서트가 존재하지 않습니다."));
    }
}