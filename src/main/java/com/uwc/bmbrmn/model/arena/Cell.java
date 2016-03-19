package com.uwc.bmbrmn.model.arena;

import java.util.concurrent.locks.Lock;

public interface Cell {

    boolean isFree();

    boolean isMovable();

    boolean isDetonatable();

    boolean isExplodable();

    Lock getLock();

}
