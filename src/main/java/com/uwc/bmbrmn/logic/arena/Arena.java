package com.uwc.bmbrmn.logic.arena;

import com.uwc.bmbrmn.model.tiles.Cell;
import com.uwc.bmbrmn.model.units.Bot;
import com.uwc.bmbrmn.model.units.Player;
import org.xguzm.pathfinding.grid.GridCell;

import java.util.Collection;
import java.util.HashSet;

public interface Arena {

    float BOX_THRESHOLD = 0.66f;
    int LOCK_TIMEOUT = 100;
    int BURNING_RADIUS = 2;
    int RESET_PLAYERS_STEPS_DURATION = 1;
    int HANDICAP_DELAY = 1000;
    int BOT_ACTION_INTERVAL = 250;

    void fillArena();

    int getWidth();

    int getHeight();

    Player getPlayer();

    Collection<Bot> getBots();

    int getTimeInSeconds();

    Cell getCellAt(int x, int y);

    void moveItem(Cell item, int deltaX, int deltaY);

    void plantBomb(Cell item);

    void detonateBomb(int x, int y);

    default Collection<Cell> getAllCells() {
        Collection<Cell> allCells = new HashSet<>(getWidth() * getHeight());
        for (int i = 0; i < getWidth(); i++) {
            for (int j = 0; j < getHeight(); j++) {
                allCells.add(getCellAt(i, j));
            }
        }
        return allCells;
    }

    default String[][] toStringArray() {
        String[][] cells = new String[getWidth()][getHeight()];
        for (int i = 0; i < getWidth(); i++) {
            for (int j = 0; j < getHeight(); j++) {
                Cell cell = getCellAt(i, j);
                cells[i][j] = cell.getId() + ":" + cell.getClass().getSimpleName();
            }
        }
        return cells;
    }

    default GridCell[][] toGridCellsArray() {
        GridCell[][] cells = new GridCell[getWidth()][getHeight()];
        for (int i = 0; i < getWidth(); i++) {
            for (int j = 0; j < getHeight(); j++) {
                cells[i][j] = new GridCell(i, j, getCellAt(i, j).isFree());
            }
        }
        return cells;
    }

    default boolean isStartCell(int i, int j) {
        return i == 0 && j == 0;
    }

    default boolean isCornerCell(int i, int j) {
        if (i == 0) if (j == 0) return true;
        if (i == 0) if (j == getHeight() - 1) return true;
        if (i == getWidth() - 1) if (j == getHeight() - 1) return true;
        if (i == getWidth() - 1) if (j == 0) return true;
        return false;
    }

    default boolean isCriticalCell(int i, int j) {
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

    default boolean isUnevenCell(int i, int j) {
        return i % 2 != 0 && j % 2 != 0;
    }

}
