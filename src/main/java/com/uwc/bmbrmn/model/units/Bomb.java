package com.uwc.bmbrmn.model.units;

import com.uwc.bmbrmn.model.arena.Arena;
import com.uwc.bmbrmn.model.arena.Cell;

public class Bomb implements Cell {

    private Arena arena;

    public Bomb(Arena arena) {
        this.arena = arena;
    }

    @Override
    public boolean isFree() {
        return false;
    }


    @Override
    public boolean isBurnable() {
        return false;
    }

}
