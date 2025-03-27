package com.example.concertreservation.domain.auth.controller;

import com.example.concertreservation.domain.auth.dto.request.SigninRequest;
import com.example.concertreservation.domain.auth.dto.request.SignupRequest;
import com.example.concertreservation.domain.auth.dto.response.SigninResponse;
import com.example.concertreservation.domain.auth.dto.response.SignupResponse;
import com.example.concertreservation.domain.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/auth/signup")
    public ResponseEntity<Void> signup(@Valid @RequestBody SignupRequest signupRequest) {
        authService.signup(signupRequest);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/auth/signin")
    public ResponseEntity<SigninResponse> signin(@Valid @RequestBody SigninRequest signinRequest) {
        return ResponseEntity.ok(authService.signin(signinRequest));
    }
}
