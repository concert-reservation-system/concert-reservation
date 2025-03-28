package com.example.concertreservation.domain.user.dto.response;

import com.example.concertreservation.common.enums.UserRole;
import com.example.concertreservation.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponse {

    private Long id;
    private String email;
    private UserRole userRole;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .userRole(user.getUserRole())
                .build();
    }
}
