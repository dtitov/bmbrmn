package com.uwc.bmbrmn.model.arena.impl;

import com.uwc.bmbrmn.model.arena.Cell;

public class Space implements Cell {

    @Override
    public boolean isFree() {
        return true;
    }


    @Override
    public boolean isBurnable() {
        return true;
    }

}
