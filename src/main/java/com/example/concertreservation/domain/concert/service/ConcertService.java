package com.example.concertreservation.domain.concert.service;

import com.example.concertreservation.common.exception.InvalidRequestException;
import com.example.concertreservation.domain.concert.dto.request.ConcertReservationPeriodRequest;
import com.example.concertreservation.domain.concert.dto.request.ConcertSaveRequest;
import com.example.concertreservation.domain.concert.dto.request.ConcertUpdateRequest;
import com.example.concertreservation.domain.concert.dto.response.ConcertResponse;
import com.example.concertreservation.domain.concert.dto.response.ConcertSaveResponse;
import com.example.concertreservation.domain.concert.entity.Concert;
import com.example.concertreservation.domain.concert.entity.ConcertReservationDate;
import com.example.concertreservation.domain.concert.repository.ConcertRepository;
import com.example.concertreservation.domain.concert.repository.ConcertReservationDateRepository;
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
    public ConcertSaveResponse saveConcert(ConcertSaveRequest saveRequest) {

        // 콘서트 저장
        Concert newConcert = Concert.builder()
                .title(saveRequest.getTitle())
                .description(saveRequest.getDescription())
                .concertDate(saveRequest.getConcertDate())
                .capacity(saveRequest.getCapacity())
                .availableAmount(saveRequest.getCapacity())
                .build();

        Concert savedConcert = concertRepository.save(newConcert);

        // 콘서트 예매 일정 저장
        ConcertReservationDate reservationDate = ConcertReservationDate.builder()
                .concert(savedConcert)
                .startDate(saveRequest.getStartDate())
                .endDate(saveRequest.getEndDate())
                .build();
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

    @Transactional
    public ConcertResponse updateConcert(long concertId, ConcertUpdateRequest concertUpdateRequest) {
        Concert concert = concertRepository.findById(concertId)
                .orElseThrow(() -> new InvalidRequestException("해당 공연이 존재하지 않습니다."));

        int oldCapacity = concert.getCapacity();
        int newCapacity = concertUpdateRequest.getCapacity();
        int oldAvailableAmount = concert.getAvailableAmount();

        int diff = newCapacity - oldCapacity;
        int newAvailableAmount = oldAvailableAmount + diff;

        if (newAvailableAmount < 0) {
            throw new InvalidRequestException("이미 예매된 좌석 수보다 적은 정원으로 변경할 수 없습니다.");
        }

        concert.update(
                concertUpdateRequest.getTitle(),
                concertUpdateRequest.getDescription(),
                concertUpdateRequest.getConcertDate(),
                concertUpdateRequest.getCapacity(),
                newAvailableAmount
        );

        return new ConcertResponse(
                concert.getId(),
                concert.getTitle(),
                concert.getDescription(),
                concert.getConcertDate(),
                concert.getCapacity()
        );
    }

    @Transactional
    public ConcertSaveResponse updateReservationPeriod(long concertId, ConcertReservationPeriodRequest concertReservationPeriodRequest) {
        Concert concert = concertRepository.findById(concertId)
                .orElseThrow(() -> new InvalidRequestException("해당 공연이 존재하지 않습니다."));

        ConcertReservationDate reservationDate = concertReservationDateRepository
                .findByConcertId(concert.getId())
                .orElseThrow(() -> new InvalidRequestException("해당 공연의 예매 일정이 존재하지 않습니다."));

        reservationDate.update(
                concertReservationPeriodRequest.getStartDate(),
                concertReservationPeriodRequest.getEndDate()
        );

        return new ConcertSaveResponse(
                concert.getId(),
                concert.getTitle(),
                concert.getDescription(),
                concert.getConcertDate(),
                concert.getCapacity(),
                reservationDate.getStartDate(),
                reservationDate.getEndDate()
        );
    }

    @Transactional
    public void deleteConcert(long concertId) {
        Concert concert = concertRepository.findById(concertId)
                .orElseThrow(() -> new InvalidRequestException("해당 공연이 존재하지 않습니다."));

        // 콘서트 예매 일정 삭제
        concertReservationDateRepository.findByConcertId(concertId)
                .ifPresent(concertReservationDateRepository::delete);

        // 콘서트 삭제
        concertRepository.delete(concert);
    }
}
