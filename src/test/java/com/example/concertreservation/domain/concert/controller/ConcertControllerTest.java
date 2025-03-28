package com.example.concertreservation.domain.concert.controller;

import com.example.concertreservation.common.dto.AuthUser;
import com.example.concertreservation.common.enums.UserRole;
import com.example.concertreservation.config.WithMockAuthUser;
import com.example.concertreservation.domain.concert.dto.request.ConcertSaveRequest;
import com.example.concertreservation.domain.concert.dto.response.ConcertDetailResponse;
import com.example.concertreservation.domain.concert.dto.response.ConcertSaveResponse;
import com.example.concertreservation.domain.concert.dto.response.ConcertSummaryResponse;
import com.example.concertreservation.domain.concert.service.ConcertService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ConcertControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockBean
    private ConcertService concertService;

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    @DisplayName("콘서트 등록 성공")
    @WithMockAuthUser(userId = 1L, email = "ex@example.com", role = UserRole.ROLE_ADMIN)
    void 콘서트_등록_성공() throws Exception {
        // given
        LocalDateTime localDateTime = LocalDateTime.now();
        ConcertSaveRequest request = ConcertSaveRequest.builder()
                .title("title")
                .description("description")
                .concertDate(localDateTime)
                .capacity(1)
                .startDate(localDateTime)
                .endDate(localDateTime)
                .build();
        ConcertSaveResponse response = ConcertSaveResponse.builder()
                .id(1L)
                .title("title")
                .description("description")
                .concertDate(localDateTime)
                .capacity(1)
                .startDate(localDateTime)
                .endDate(localDateTime)
                .build();

        when(concertService.saveConcert(any(ConcertSaveRequest.class))).thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/concerts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @DisplayName("콘서트 단건 조회 성공")
    @WithMockAuthUser(userId = 1L, email = "ex@example.com", role = UserRole.ROLE_USER)
    void 콘서트_단건_조회_성공() throws Exception {
        // given
        long concertId = 1L;
        ConcertDetailResponse response = new ConcertDetailResponse(
                concertId,
                "title",
                "description",
                LocalDateTime.now(),
                1,
                1,
                1,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(concertService.getConcert(eq(concertId), any(AuthUser.class)))
                .thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/concerts/{concertId}", concertId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(concertId));
    }

    @Test
    @DisplayName("콘서트 목록 조회 성공")
    @WithMockAuthUser(userId = 1L, email = "ex@example.com", role = UserRole.ROLE_USER)
    void 콘서트_목록_조회_성공() throws Exception {
        // given
        ConcertSummaryResponse concert1 = new ConcertSummaryResponse(1L, "title1", LocalDateTime.now());
        ConcertSummaryResponse concert2 = new ConcertSummaryResponse(2L, "title2", LocalDateTime.now());

        List<ConcertSummaryResponse> concerts = List.of(concert1, concert2);
        Page<ConcertSummaryResponse> pageResponse = new PageImpl<>(concerts);

        when(concertService.getConcerts(anyInt(), anyInt(), any(), any(), any()))
                .thenReturn(pageResponse);

        // when & then
        mockMvc.perform(get("/api/concerts")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(1L));
    }

    @Test
    @DisplayName("콘서트 삭제 성공")
    @WithMockAuthUser(userId = 1L, email = "admin@example.com", role = UserRole.ROLE_ADMIN)
    void 콘서트_삭제_성공() throws Exception {
        // given
        long concertId = 1L;

        // when & then
        mockMvc.perform(delete("/api/concerts/{concertId}", concertId))
                .andExpect(status().isOk())
                .andExpect(content().string("콘서트 삭제 성공"));
    }
}