package com.example.concertreservation.common.fairlock;

public interface LockManager {
    void executeWithLock(String key, Runnable task) throws InterruptedException;
}
