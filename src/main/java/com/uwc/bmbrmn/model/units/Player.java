package com.uwc.bmbrmn.model.units;

import com.uwc.bmbrmn.model.arena.impl.AbstractCell;

public class Player extends AbstractCell {

    public Player(int x, int y) {
        super(x, y);
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
    public boolean isExplodable() {
        return true;
    }

}
