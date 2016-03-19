package com.uwc.bmbrmn.model.units;

import com.uwc.bmbrmn.model.arena.Navigable;
import com.uwc.bmbrmn.model.arena.impl.SyncronizedCell;

public class Bomb extends SyncronizedCell implements Navigable {

    private int x;
    private int y;

    public Bomb(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean isFree() {
        return false;
    }

    @Override
    public boolean isMovable() {
        return false;
    }

    @Override
    public boolean isDetonatable() {
        return true;
    }

    @Override
    public boolean isExplodable() {
        return false;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public void setX(int x) {
        this.x = x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public void setY(int y) {
        this.y = y;
    }

}
