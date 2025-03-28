package com.example.concertreservation.domain.user.controller;

import com.example.concertreservation.common.dto.AuthUser;
import com.example.concertreservation.domain.user.dto.request.ChangePasswordRequest;
import com.example.concertreservation.domain.user.dto.response.UserResponse;
import com.example.concertreservation.domain.user.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/users/{userId}")
    public ResponseEntity<UserResponse> findUser(@PathVariable Long userId){

        return new ResponseEntity<>(userService.find(userId), HttpStatus.OK);
    }

    @GetMapping("/users")
    public ResponseEntity<Page<UserResponse>> findAllUser(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction
    ){

        return new ResponseEntity<>(userService.findAll(page, size, direction),HttpStatus.OK);
    }

    @PatchMapping("/users/{userId}")
    public ResponseEntity<Void> changePassword(
            @PathVariable Long userId,
            @AuthenticationPrincipal AuthUser authUser,
            @Valid ChangePasswordRequest request){

        userService.changePassword(userId, authUser, request);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long userId,
            @AuthenticationPrincipal AuthUser authUser){

        userService.deleteUser(authUser, userId);

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
