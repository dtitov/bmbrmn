package com.uwc.bmbrmn.model.tiles.impl;

public class Box extends AbstractCell {

    public Box(int x, int y) {
        super(x, y);
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
    public boolean isExplodable() {
        return true;
    }

}
