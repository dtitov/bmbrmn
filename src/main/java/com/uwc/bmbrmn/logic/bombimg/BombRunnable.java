package com.uwc.bmbrmn.logic.bombimg;

import com.uwc.bmbrmn.logic.arena.Arena;
import com.uwc.bmbrmn.model.tiles.Cell;
import com.uwc.bmbrmn.scheduling.RequestAwareRunnable;

import java.util.concurrent.Future;

import static com.uwc.bmbrmn.logic.bombimg.BombManager.DETONATION_DELAY;

public class BombRunnable extends RequestAwareRunnable {

    private Arena arena;
    private BombManager bombManager;
    private String id;
    private int x;
    private int y;
    private Future<?> future;

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
            if (!future.isDone() && !future.isCancelled()) {
                arena.detonateBomb(x, y);
                bombManager.decrementBombsUsed(id);
                future.cancel(false);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void setFuture(Future<?> future) {
        this.future = future;
    }

}
