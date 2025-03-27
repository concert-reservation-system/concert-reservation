package com.example.concertreservation.domain.reservation;

import com.example.concertreservation.common.enums.UserRole;
import com.example.concertreservation.domain.concert.entity.Concert;
import com.example.concertreservation.domain.concert.entity.ConcertReservationDate;
import com.example.concertreservation.domain.concert.repository.ConcertRepository;
import com.example.concertreservation.domain.concert.repository.ConcertReservationDateRepository;
import com.example.concertreservation.domain.user.entity.User;
import com.example.concertreservation.domain.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

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
        concert = Concert.builder()
                .title("테스트용 콘서트")
                .capacity(100)
                .availableAmount(100)
                .build();
        concertRepository.saveAndFlush(concert);

        concertReservationDateRepository.saveAndFlush(
                ConcertReservationDate.builder()
                        .concert(concert)
                        .startDate(LocalDateTime.of(2025, 3, 1, 0, 0))
                        .endDate(LocalDateTime.of(2025, 3, 31, 23, 59))
                        .build()
        );

        user = User.builder()
                .email("testuser@example.com")
                .password("1234")
                .userRole(UserRole.ROLE_USER)
                .build();
        userRepository.saveAndFlush(user);
    }

    @AfterEach
    public void tearDown() {
        userRepository.deleteAll();
        concertReservationDateRepository.deleteAll();
        concertRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "user1", roles = "USER")
    public void 콘서트_예약_요청() throws Exception {
        // 예약 요청 성공 테스트
        mockMvc.perform(post("/reservations/{concertId}", concert.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string("예약이 완료되었습니다."));
    }

    @Test
    public void 로그인되지않은_사용자_예약_요청() throws Exception {
        // 인증되지 않은 경우
        mockMvc.perform(post("/reservations/{concertId}", concert.getId()))
                .andExpect(status().isForbidden())
                .andExpect(content().string("로그인이 필요합니다."));
    }

    @Test
    @WithMockUser(username = "user1", roles = "USER")
    public void 비존재_콘서트_예약_요청() throws Exception {
        // 존재하지 않는 콘서트에 예약 시도
        Long fakeConcertId = 7777L;

        mockMvc.perform(post("/reservations/{concertId}", fakeConcertId))
                .andExpect(status().isNotFound())
                .andExpect(content().string("콘서트를 찾을 수 없습니다."));
    }
}