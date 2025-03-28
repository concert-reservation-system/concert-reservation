package com.example.concertreservation.common.lock.redisson;

public interface LockManager {
    void executeWithLock(String key, Runnable task) throws InterruptedException;
}