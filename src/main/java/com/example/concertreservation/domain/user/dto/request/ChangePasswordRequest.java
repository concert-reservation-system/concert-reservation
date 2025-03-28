package com.example.concertreservation.domain.user.dto.request;

import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
public class ChangePasswordRequest {

    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{4,}$")
    private String oldPassword;
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{4,}$")
    private String newPassword;

    public ChangePasswordRequest(String oldPassword, String newPassword) {
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
    }
}
