package com.example.concertreservation.domain.concert.service;

import com.example.concertreservation.domain.concert.dto.request.ConcertSaveRequest;
import com.example.concertreservation.domain.concert.dto.response.ConcertSaveResponse;
import com.example.concertreservation.domain.concert.entity.Concert;
import com.example.concertreservation.domain.concert.entity.ConcertReservationDate;
import com.example.concertreservation.domain.concert.repository.ConcertRepository;
import com.example.concertreservation.domain.concert.repository.ConcertReservationDateRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConcertService {

    private final ConcertRepository concertRepository;
    private final ConcertReservationDateRepository concertReservationDateRepository;

    @Transactional
    public ConcertSaveResponse saveConcert(@Valid ConcertSaveRequest saveRequest) {

        // 콘서트 저장
        Concert newConcert = new Concert(
                saveRequest.getTitle(),
                saveRequest.getDescription(),
                saveRequest.getConcertDate(),
                saveRequest.getCapacity()
        );
        Concert savedConcert = concertRepository.save(newConcert);

        // 콘서트 예매 일정 저장
        ConcertReservationDate reservationDate = new ConcertReservationDate(
                savedConcert,
                saveRequest.getStartDate(),
                saveRequest.getEndDate()
        );
        ConcertReservationDate savedReservationDate = concertReservationDateRepository.save(reservationDate);

        return new ConcertSaveResponse(
                savedConcert.getId(),
                savedConcert.getTitle(),
                savedConcert.getDescription(),
                savedConcert.getConcertDate(),
                savedConcert.getCapacity(),
                savedReservationDate.getStartDate(),
                savedReservationDate.getEndDate()
        );
    }
}
