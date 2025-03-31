package com.example.concertreservation.domain.concert.service;

import com.example.concertreservation.common.utils.RandomCreator;
import com.example.concertreservation.domain.concert.entity.Concert;
import com.example.concertreservation.domain.concert.entity.ConcertReservationDate;
import com.example.concertreservation.domain.concert.repository.ConcertRepository;
import com.example.concertreservation.domain.concert.repository.ConcertReservationDateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class ConcertDataCreator {
    private final RandomCreator randomCreator;
    private final ConcertRepository concertRepository;
    private final ConcertReservationDateRepository concertReservationDateRepository;
    private static final int TOTAL_CONCERT = 100;

    public void createConcerts() {
        Random random = new Random();

        List<Concert> concerts = new ArrayList<>();
        List<ConcertReservationDate> concertReservationDates = new ArrayList<>();

        for (int j = 0; j < TOTAL_CONCERT; j++) {
            String title = randomCreator.generateTitle();
            String description = title + " 설명";
            Timestamp concertTimestamp = randomCreator.generateRandomFutureTimestamp();
            int capacity = 1000 * (random.nextInt(5) + 1);
            int availableAmount = capacity;
            int viewCount = random.nextInt(100);

            Concert concert = new Concert(title, description, concertTimestamp.toLocalDateTime(), capacity, availableAmount, viewCount);
            concerts.add(concert);
        }
        concertRepository.saveAll(concerts);

        for (Concert concert : concerts) {
            LocalDateTime concertDate = concert.getConcertDate();
            LocalDateTime startDate = concertDate.minusMonths(2);
            LocalDateTime endDate = startDate.plusDays(7);
            ConcertReservationDate concertReservationDate = new ConcertReservationDate(concert, startDate, endDate);
            concertReservationDates.add(concertReservationDate);
        }
        concertReservationDateRepository.saveAll(concertReservationDates);
    }
}
