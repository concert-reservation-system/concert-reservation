package com.example.concertreservation.domain.user.controller;

import com.example.concertreservation.common.enums.UserRole;
import com.example.concertreservation.common.exception.InvalidRequestException;
import com.example.concertreservation.config.JwtUtil;
import com.example.concertreservation.config.WithMockAuthUser;
import com.example.concertreservation.domain.user.dto.request.ChangePasswordRequest;
import com.example.concertreservation.domain.user.dto.response.UserResponse;
import com.example.concertreservation.domain.user.entity.User;
import com.example.concertreservation.domain.user.repository.UserRepository;
import com.example.concertreservation.domain.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @BeforeEach
    public void setUp() {
    }

    @Test
    @DisplayName("사용자 단일 조회")
    @WithMockAuthUser(userId = 1L, email = "test1@tset.com", role = UserRole.ROLE_USER)
    public void findUser() throws Exception {
        User user = User.builder()
                .email("test1@test.com")
                .password("test12345")
                .userRole(UserRole.ROLE_USER)
                .build();
        UserResponse response = UserResponse.from(user);

        when(userService.find(1L)).thenReturn(response);

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test1@test.com"));
    }

    @Test
    @DisplayName("존재하지 않는 사용자 조회")
    @WithMockAuthUser(userId = 1L, email = "test1@tset.com", role = UserRole.ROLE_USER)
    public void findUnknown() throws Exception {
        when(userService.find(1234L)).thenThrow(new InvalidRequestException("존재하지 않는 회원입니다."));

        mockMvc.perform(get("/users/1234"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("존재하지 않는 회원입니다."));
    }

    @Test
    @DisplayName("모든 사용자 조회")
    @WithMockAuthUser(userId = 1L, email = "test1@tset.com", role = UserRole.ROLE_USER)
    void findAllUser() throws Exception {
        User user = User.builder()
                .email("test1@test.com")
                .password("test12345")
                .userRole(UserRole.ROLE_USER)
                .build();
        Page<UserResponse> pages = new PageImpl<>(List.of(UserResponse.from(user)));

        when(userService.findAll(1, 10, Sort.Direction.DESC))
                .thenReturn(pages);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].email").value("test1@test.com"));
    }

    @Test
    @WithMockAuthUser(userId = 1L, email = "test1@tset.com", role = UserRole.ROLE_USER)
    @DisplayName("비밀번호 변경 테스트")
    void changePassword() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest("testtest1", "newtesttest1");

        mockMvc.perform(patch("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockAuthUser(userId = 1L, email = "test1@tset.com", role = UserRole.ROLE_USER)
    @DisplayName("사용자 삭제 테스트")
    void deleteUser() throws Exception {
        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isOk());
    }
}