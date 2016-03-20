package com.uwc.bmbrmn.model.arena;

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

    int getX();

    void setX(int x);

    int getY();

    void setY( int y);

    @JsonIgnore
    Lock getLock();

    @JsonGetter("type")
    default String getType() {
        return getClass().getSimpleName();
    }

}
