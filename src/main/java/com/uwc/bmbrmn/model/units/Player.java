package com.uwc.bmbrmn.model.units;

import com.uwc.bmbrmn.model.arena.Cell;

public class Player implements Cell {

    private int x;
    private int y;

    @Override
    public boolean isFree() {
        return false;
    }

    @Override
    public boolean isBurnable() {
        return true;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
}
