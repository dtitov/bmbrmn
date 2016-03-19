package com.uwc.bmbrmn.model.arena.impl;

import com.uwc.bmbrmn.model.arena.Cell;

public class Block implements Cell {

    @Override
    public boolean isFree() {
        return false;
    }

    @Override
    public boolean isBurnable() {
        return false;
    }

}
