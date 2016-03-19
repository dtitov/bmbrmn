package com.uwc.bmbrmn.model.arena.impl;

import com.uwc.bmbrmn.model.arena.Cell;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class SyncronizedCell implements Cell {

    private Lock lock = new ReentrantLock();

    @Override
    public Lock getLock() {
        return lock;
    }

}
