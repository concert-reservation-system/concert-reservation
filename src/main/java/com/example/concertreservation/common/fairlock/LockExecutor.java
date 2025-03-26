package com.example.concertreservation.common.fairlock;

public interface LockExecutor {
    void execute(String key, Runnable task) throws InterruptedException;
}
