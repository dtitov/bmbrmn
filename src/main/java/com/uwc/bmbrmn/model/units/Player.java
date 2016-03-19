package com.uwc.bmbrmn.model.units;

import com.uwc.bmbrmn.model.arena.Navigable;
import com.uwc.bmbrmn.model.arena.impl.SyncronizedCell;

public class Player extends SyncronizedCell implements Navigable {

    private int x;
    private int y;

    public Player(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean isFree() {
        return false;
    }

    @Override
    public boolean isMovable() {
        return true;
    }

    @Override
    public boolean isDetonatable() {
        return false;
    }

    @Override
    public boolean isExplodable() {
        return true;
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
