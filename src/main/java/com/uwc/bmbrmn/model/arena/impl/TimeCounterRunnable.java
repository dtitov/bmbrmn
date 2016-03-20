package com.uwc.bmbrmn.model.arena.impl;

import java.util.concurrent.atomic.AtomicInteger;

public class TimeCounterRunnable implements Runnable {

    private AtomicInteger gameSecond;

    public TimeCounterRunnable(AtomicInteger gameSecond) {
        this.gameSecond = gameSecond;
    }

    @Override
    public void run() {
        gameSecond.incrementAndGet();
    }

}
