package com.example.concertreservation.domain.user.dto.response;

import lombok.Getter;

@Getter
public class UserResponse {

    private String email;

    public UserResponse(String email) {
        this.email = email;
    }
}
