package com.example.concertreservation.domain.user.entity;

import com.example.concertreservation.common.entity.BaseTimeEntity;
import com.example.concertreservation.common.enums.UserRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "users")
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String email;
    private String password;
    @Enumerated(EnumType.STRING)
    private UserRole userRole;
//    private LocalDateTime deletedAt = null;

    @Builder
    public User(String email, String password, UserRole userRole) {
        this.email = email;
        this.password = password;
        this.userRole = userRole;
    }

//    public void deleteUser(){
//        this.deletedAt = LocalDateTime.now();
//    }

//    public void changePassword(String password) {
//        this.password = password;
//    }

    public User(String email) {
        this.email = email;
    }
}
