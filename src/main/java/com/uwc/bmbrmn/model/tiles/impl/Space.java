package com.uwc.bmbrmn.model.tiles.impl;

public class Space extends AbstractCell {

    public Space(int x, int y) {
        super(x, y);
    }

    @Override
    public boolean isFree() {
        return !isMined();
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
