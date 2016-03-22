package com.uwc.bmbrmn.model.units;

import com.uwc.bmbrmn.model.tiles.impl.AbstractCell;

import java.util.concurrent.atomic.AtomicInteger;

public class Player extends AbstractCell {

    private volatile AtomicInteger stepsDone = new AtomicInteger(0);
    private volatile boolean alive;

    public Player(int x, int y) {
        super(x, y);
        alive = true;
    }

    @Override
    public boolean isFree() {
        return false;
    }

    @Override
    public boolean isMovable() {
        return stepsDone.get() < 4;
    }

    @Override
    public boolean isExplodable() {
        return true;
    }

    @Override
    public void move(int x, int y) {
        super.move(x, y);
        stepsDone.incrementAndGet();
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public void resetSteps() {
        stepsDone.set(0);
    }

}
