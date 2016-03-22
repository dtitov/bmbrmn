package com.uwc.bmbrmn.model.tiles.impl;

import com.uwc.bmbrmn.model.tiles.Cell;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class AbstractCell implements Cell {

    private volatile String id;
    private volatile boolean mined;
    private volatile boolean flaming;
    private volatile int x;
    private volatile int y;

    private Lock lock = new ReentrantLock();

    public AbstractCell(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String getId() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        return id;
    }

    @Override
    public boolean isMined() {
        return mined;
    }

    public void setMined(boolean mined) {
        this.mined = mined;
    }

    @Override
    public boolean isFlaming() {
        return flaming;
    }

    public void setFlaming(boolean flaming) {
        this.flaming = flaming;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public void move(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public Lock getLock() {
        return lock;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbstractCell that = (AbstractCell) o;

        return id.equals(that.id);

    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    @Override
    public String toString() {
        return getType() + "{" +
                "id='" + id + '\'' +
                ", x=" + x +
                ", y=" + y +
                '}';
    }
}
