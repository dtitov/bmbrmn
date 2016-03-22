package com.uwc.bmbrmn.model.tiles;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.concurrent.locks.Lock;

public interface Cell {

    String getId();

    @JsonIgnore
    boolean isFree();

    @JsonIgnore
    boolean isMovable();

    @JsonIgnore
    boolean isExplodable();

    boolean isMined();

    void setMined(boolean mined);

    boolean isFlaming();

    void setFlaming(boolean flaming);

    int getX();

    int getY();

    void move(int x, int y);

    @JsonIgnore
    Lock getLock();

    @JsonGetter("type")
    default String getType() {
        return getClass().getSimpleName();
    }

}
