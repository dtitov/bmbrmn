package com.uwc.bmbrmn.logic.bombimg;

import com.uwc.bmbrmn.model.arena.Arena;
import com.uwc.bmbrmn.model.arena.Cell;

import static com.uwc.bmbrmn.logic.bombimg.BombManager.DETONATION_DELAY;

public class BombRunnable extends RequestAwareRunnable {

    private Arena arena;
    private BombManager bombManager;
    private String id;
    private int x;
    private int y;

    public BombRunnable(Arena arena, BombManager bombManager, Cell cell) {
        this.arena = arena;
        this.bombManager = bombManager;
        this.id = cell.getId();
        this.x = cell.getX();
        this.y = cell.getY();
    }

    @Override
    protected void onRun() {
        try {
            Thread.sleep(DETONATION_DELAY);
            arena.detonateBomb(x, y);
            bombManager.decrementBombsUsed(id);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
