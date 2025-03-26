# PickT (Concert Ticketing Application)
대량 요청 속에서도 데이터 정합성을 보장하는 콘서트 티켓팅 시스템

## Project Overview
- 순간적으로 수천 건의 요청이 몰리는 상황에서도 티켓 수량 초과 없이 처리
- Redis 분산 락 / AOP / 테스트 코드를 통한 동시성 제어 구현
- 캐싱/검색 최적화를 통한 성능 개선

## Key Features

| 이름 | 담당 기능 |
|---|---|
| 김한나 | 콘서트 등록/조회/수정/삭제 + JPA + QueryDSL + 캐시 성능 개선 |
| 권호준 | 동시성 테스트 코드 작성 |
| 김대정 | 회원가입/로그인 기능 + AOP 락 적용 |
| 최유리 | Redis Lettuce 락 구현 및 동시성 이슈 제어 |
| 김제인 | Redisson FairLock 적용 |

## Tech Stack
- **Language:** Java 17
- **Framework:** Spring Boot 3.x
- **ORM:** Spring Data JPA + QueryDSL
- **DB:** MySQL
- **Cache/Lock:** Redis (Lettuce, Redisson)
- **Security:** Spring Security + JWT
- **Build Tool:** Gradle

## ERD
![Image](https://github.com/user-attachments/assets/542b811f-1290-4928-86e5-c5de2748da3b)