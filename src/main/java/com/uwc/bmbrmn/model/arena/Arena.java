package com.uwc.bmbrmn.model.arena;

public interface Arena {

    double BOX_THRESHOLD = 0.66;

    void fillArena();

    String[][] toArray();

    int getWidth();

    int getHeight();

    default boolean isStart(int i, int j) {
        return i == 0 && j == 0;
    }

    default boolean isCorner(int i, int j) {
        if (i == 0) if (j == 0) return true;
        if (i == 0) if (j == getHeight()) return true;
        if (i == getWidth()) if (j == getHeight()) return true;
        if (i == getWidth()) if (j == 0) return true;
        return false;
    }

    default boolean isBlock(int i, int j) {
        return i % 2 != 0 && j % 2 != 0;
    }

}
