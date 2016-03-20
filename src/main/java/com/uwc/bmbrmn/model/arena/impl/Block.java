package com.uwc.bmbrmn.model.arena.impl;

public class Block extends AbstractCell {

    public Block(int x, int y) {
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
        return false;
    }

}
