package com.example.concertreservation.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class SigninRequest {

    @Email
    @NotNull
    private String email;
    @NotNull
    private String password;
}
