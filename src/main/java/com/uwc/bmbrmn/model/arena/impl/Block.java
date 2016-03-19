package com.uwc.bmbrmn.model.arena.impl;

public class Block extends SyncronizedCell {

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
        return false;
    }

    @Override
    public boolean isExplodable() {
        return false;
    }

}
