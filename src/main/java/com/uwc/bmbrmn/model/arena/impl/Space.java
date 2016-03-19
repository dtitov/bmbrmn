package com.uwc.bmbrmn.model.arena.impl;

public class Space extends SyncronizedCell {

    @Override
    public boolean isFree() {
        return true;
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
        return true;
    }

}
