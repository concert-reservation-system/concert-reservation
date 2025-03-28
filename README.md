# PickT (Concert Ticketing Application)
대량 요청 속에서도 데이터 무결성과 정합성을 보장하는 콘서트 티켓팅 시스템

## Project Overview
- 많은 사용자가 몰리는 상황에서도 티켓 수량 초과 없이 처리
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
<img src="https://img.shields.io/badge/java-007396?style=flat-square&logo=java&logoColor=white"/>
<img src="https://img.shields.io/badge/MySQL-4479A1?style=flat-square&logo=MySQL&logoColor=white"/>
<img src="https://img.shields.io/badge/Postman-FF6C37?style=flat-square&logo=Postman&logoColor=white"/>
<img src="https://img.shields.io/badge/Spring-6DB33F?style=flat-square&logo=Spring&logoColor=white"/>
<img src="https://img.shields.io/badge/Redis-6DB33F?style=flat-square&logo=Redis&logoColor=white"/>

## ERD
![Image](https://github.com/user-attachments/assets/542b811f-1290-4928-86e5-c5de2748da3b)

## Concurrency Control & Lock
- 콘서트 예약 시작 시간에 많은 요청이 동시에 몰릴 수 있음
- 제어하지 않으면 오버셀링 , 중복 예매 , 데이터 무결성 문제 발생

## Lock

| Lock 방식 | 적용자 | 장점 | 단점 | 적용 이유 |
|---|---|---|---|----|
| Lettuce 기반 Lock | 최유리 | 비동기 처리 가능, 구현 간단 | 공정성 없음 , Spin Lock 방식으로 CPU 자원이 많이 듬 | Redis 분산 락 테스트 목적 |
| Spring AOP Lock | 김대정 | 횡단 관심사 분리 용이 | 단일 서버 환경에 적합 | 코드 구조화 , 진입 시점 통일 |
| Redisson Fair Lock | 김제인 | 공정성 보장 (요청 순서대로 처리) , 안정성 높음 | 구현 복잡도 , 성능 저하 가능성 | 티켓팅처럼 순차적 처리가 중요한 서비스에 적합 |
| Optimistic Lock | | 읽기 성능 우수, 락을 걸지 않음 | 대량의 쓰기 작업 시 비효율적, 분산환경 제한적 | 조회가 많고 충돌이 적은 환경 |
| Pessimistic Lock | | 쓰기 성능 우수, 충돌 방지 | 트랜잭션 대기로 인한 성능 저하, 데드락 발생 가능 | 충돌이 빈번한 환경 |
- 예매 순서가 중요한 서비스 -> 선착순 티켓팅
- 대기 큐를 관리하며 요청 순서 보장

## Redis Cache
- 조회 성능 개선 (예약 가능한 콘서트 목록 등)
- 만료시간 (TTL) 설정으로 데이터 최신성 보장

## Technical Challenges Solved
- Redis 기반 분산 락으로 재고 초과 문제 방지
- AOP 구조로 락 적용 범위 통일
- Redisson FairLock으로 요청 순서 보장
- 동시성 테스트로 락의 유효성 검증
