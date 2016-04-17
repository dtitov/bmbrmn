package com.uwc.bmbrmn.logic.bombimg;

import com.uwc.bmbrmn.logic.arena.Arena;
import com.uwc.bmbrmn.model.tiles.Cell;
import com.uwc.bmbrmn.scheduling.SessionAwareRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Future;

import static com.uwc.bmbrmn.logic.bombimg.BombManager.DETONATION_DELAY;

/**
 * Runnable to detonate bomb
 */
public class BombRunnable extends SessionAwareRunnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(BombRunnable.class);

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

    /**
     * Detonates bomb after constant delay, switches off runnable after detonation
     */
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
            LOGGER.error(e.getMessage(), e);
        }
    }

    public void setFuture(Future<?> future) {
        this.future = future;
    }

}
