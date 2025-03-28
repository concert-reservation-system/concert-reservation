package com.example.concertreservation.domain.concert.util;

public class RedisKey {
    public static final String CONCERT_VIEW_COUNT = "concert:view:";        // + concertId
    public static final String CONCERT_USER_VIEW = "concert:view:user:";    // + concertId:userId
    public static final String CONCERT_VIEW_RANKING = "concert:view:ranking";
}
