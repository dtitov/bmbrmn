package com.uwc.bmbrmn.model.arena;

import com.uwc.bmbrmn.model.units.Player;

public interface Arena {

    int LOCK_TIMEOUT = 100;
    float BOX_THRESHOLD = 0.66f;

    void fillArena();

    int getWidth();

    int getHeight();

    Player getPlayer();

    void moveItem(Cell item, int deltaX, int deltaY);

    void plantBomb(Cell item);

    void detonateBomb(Cell item);

    String[][] toArray();

    default boolean isStart(int i, int j) {
        return i == 0 && j == 0;
    }

    default boolean isCorner(int i, int j) {
        if (i == 0) if (j == 0) return true;
        if (i == 0) if (j == getHeight() - 1) return true;
        if (i == getWidth() - 1) if (j == getHeight() - 1) return true;
        if (i == getWidth() - 1) if (j == 0) return true;
        return false;
    }

    default boolean isCriticalPoint(int i, int j) {
        if (i == 1) if (j == 0) return true;
        if (i == 0) if (j == 1) return true;
        if (i == getWidth() - 1) if (j == 1) return true;
        if (i == getWidth() - 2) if (j == 0) return true;
        if (i == 0) if (j == getHeight() - 2) return true;
        if (i == 1) if (j == getHeight() - 1) return true;
        if (i == getWidth() - 1) if (j == getHeight() - 2) return true;
        if (i == getWidth() - 2) if (j == getHeight() - 1) return true;
        return false;
    }

    default boolean isBlock(int i, int j) {
        return i % 2 != 0 && j % 2 != 0;
    }

}
