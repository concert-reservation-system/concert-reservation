package com.example.concertreservation.domain.user.service;

import com.example.concertreservation.common.dto.AuthUser;
import com.example.concertreservation.common.exception.InvalidAuthenticationException;
import com.example.concertreservation.common.exception.InvalidRequestException;
import com.example.concertreservation.domain.user.dto.request.ChangePasswordRequest;
import com.example.concertreservation.domain.user.dto.response.UserResponse;
import com.example.concertreservation.domain.user.entity.User;
import com.example.concertreservation.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public UserResponse find(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidRequestException("존재하지 않는 회원입니다."));

        return new UserResponse(user.getEmail());
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> findAll(int page, int size, Sort.Direction direction) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(direction, "creatAt"));

        Page<User> pages = userRepository.findAll(pageable);

        return pages.map(user -> new UserResponse(
                user.getEmail()
        ));
    }

    @Transactional
    public void changePassword(Long userId, AuthUser authUser, ChangePasswordRequest request) {

        User user = userRepository.findById(authUser.getId())
                .orElseThrow(() -> new InvalidRequestException("존재하지 않는 회원입니다."));

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new InvalidRequestException("새 비밀번호는 기존 비밀번호와 같을 수 없습니다.");
        }

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new InvalidRequestException("잘못된 비밀번호입니다.");
        }

//        user.changePassword(passwordEncoder.encode(request.getNewPassword()));
    }

    @Transactional
    public void deleteUser(AuthUser authUser, Long userId) {

        User user = userRepository.findById(authUser.getId())
                .orElseThrow(() -> new InvalidRequestException("존재하지 않는 회원입니다."));

        if(!user.getId().equals(userId)){
            throw new InvalidAuthenticationException("본인 계정이 아닙니다.");
        }

//        userRepository.delete(user);
//        user.deleteUser();;
    }
}
