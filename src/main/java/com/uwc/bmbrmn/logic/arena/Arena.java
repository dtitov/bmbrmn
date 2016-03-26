package com.uwc.bmbrmn.logic.arena;

import com.googlecode.concurentlocks.CompositeLock;
import com.uwc.bmbrmn.model.tiles.Cell;
import com.uwc.bmbrmn.model.units.Bot;
import com.uwc.bmbrmn.model.units.Player;
import org.xguzm.pathfinding.grid.GridCell;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.locks.Lock;

/**
 * Interface for representing game board
 */
public interface Arena {

    float BOX_THRESHOLD = 0.66f;
    int LOCK_TIMEOUT = 200;
    int BURNING_RADIUS = 2;
    int RESET_PLAYERS_STEPS_DURATION = 1;
    int HANDICAP_DELAY = 1000;
    int BOT_ACTION_INTERVAL = 250;

    /**
     * Get arena width
     *
     * @return Width of arena
     */
    int getWidth();

    /**
     * Get arena height
     *
     * @return Height of arena
     */
    int getHeight();

    /**
     * Get instance of player (human-controlled unit)
     *
     * @return Instance of player
     */
    Player getPlayer();

    /**
     * Get collection of bots (AI controlled units)
     *
     * @return Collection of bots
     */
    Collection<Bot> getBots();

    /**
     * Get in-game time
     *
     * @return Number of seconds passed since game beginning
     */
    int getTimeInSeconds();

    /**
     * Get instance of cell located at specified coordinates
     *
     * @param x x coordinate
     * @param y y coordinate
     * @return Instance of cell located at specified coordinates
     */
    Cell getCellAt(int x, int y);

    /**
     * Move item by specified delta
     *
     * @param item   Item to move
     * @param deltaX x delta
     * @param deltaY y delta
     */
    void moveItem(Cell item, int deltaX, int deltaY);

    /**
     * Plant bomb if it's possible
     *
     * @param item Cell where bomb should be planted
     */
    void plantBomb(Cell item);

    /**
     * Detonate bomb located at specified coordinates
     *
     * @param x x coordinate
     * @param y y coordinate
     */
    void detonateBomb(int x, int y);

    /**
     * Get all cells of current map
     *
     * @return Collection of all existing cells
     */
    default Collection<Cell> getAllCells() {
        Collection<Cell> allCells = new HashSet<>(getWidth() * getHeight());
        for (int i = 0; i < getWidth(); i++) {
            for (int j = 0; j < getHeight(); j++) {
                allCells.add(getCellAt(i, j));
            }
        }
        return allCells;
    }

    /**
     * Get global CompositeLock
     *
     * @return CompositeLock for all cell of the map
     */
    default CompositeLock getMapLock() {
        return getCompositeLock(getAllCells());
    }

    /**
     * Get CompositeLock for specified cells
     *
     * @param cells Cells to get lock for
     * @return CompositeLock for specified cells
     */
    default CompositeLock getCompositeLock(Cell... cells) {
        return getCompositeLock(Arrays.asList(cells));
    }

    /**
     * Get CompositeLock for specified cells
     *
     * @param cells Cells to get lock for
     * @return CompositeLock for specified cells
     */
    default CompositeLock getCompositeLock(Collection<Cell> cells) {
        Lock[] locks = cells.stream().map(Cell::getLock).toArray(Lock[]::new);
        return new CompositeLock(locks);
    }

    /**
     * Converts map to 2D array of Strings
     *
     * @return String representation of map
     */
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

    /**
     * Converts map to 2D array of GridCells
     *
     * @return GridCells representation of map
     */
    default GridCell[][] toGridCellsArray() {
        GridCell[][] cells = new GridCell[getWidth()][getHeight()];
        for (int i = 0; i < getWidth(); i++) {
            for (int j = 0; j < getHeight(); j++) {
                cells[i][j] = new GridCell(i, j, getCellAt(i, j).isFree());
            }
        }
        return cells;
    }

    /**
     * Check if it's start cell (0, 0)
     *
     * @param i x coordinate
     * @param j y coordinate
     * @return true if it's start cell (0, 0), false otherwise
     */
    default boolean isStartCell(int i, int j) {
        return i == 0 && j == 0;
    }

    /**
     * Check if it's corner cell
     *
     * @param i x coordinate
     * @param j y coordinate
     * @return true if it's corner cell, false otherwise
     */
    default boolean isCornerCell(int i, int j) {
        if (i == 0) if (j == 0) return true;
        if (i == 0) if (j == getHeight() - 1) return true;
        if (i == getWidth() - 1) if (j == getHeight() - 1) return true;
        if (i == getWidth() - 1) if (j == 0) return true;
        return false;
    }

    /**
     * Check if it's 'critical' cell: cells where boxes can't be placed, because they'll make bot or player stuck in initial position
     *
     * @param i x coordinate
     * @param j y coordinate
     * @return true if it's 'critical' cell, false otherwise
     */
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

    /**
     * Check if it's odd cell
     *
     * @param i x coordinate
     * @param j y coordinate
     * @return true if it's odd cell, false otherwise
     */
    default boolean isUnevenCell(int i, int j) {
        return i % 2 != 0 && j % 2 != 0;
    }

}
