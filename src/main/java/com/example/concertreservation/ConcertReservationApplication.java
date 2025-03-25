package com.example.concertreservation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class ConcertReservationApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConcertReservationApplication.class, args);
    }
}