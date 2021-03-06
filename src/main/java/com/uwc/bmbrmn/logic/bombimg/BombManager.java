package com.uwc.bmbrmn.logic.bombimg;

import com.uwc.bmbrmn.logic.arena.Arena;
import com.uwc.bmbrmn.model.tiles.Cell;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service for managing planted bombs
 */
@Service
@Scope(scopeName = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class BombManager {

    public static final int BOMB_INCREMENT_INTERVAL = 30;
    public static final int DETONATION_DELAY = 3000;

    @Autowired
    protected Arena arena;

    private Map<String, AtomicInteger> bombMap = new ConcurrentHashMap<>(8);

    private ExecutorService executorService;

    /**
     * Init ThreadPool for BombRunnables
     */
    @PostConstruct
    public void init() {
        executorService = Executors.newFixedThreadPool(arena.getWidth() * arena.getHeight());
    }

    /**
     * Try to plant bomb at specified cell
     *
     * @param player cell to plant bomb (Player or Bot)
     * @return true if bomb was planned, false otherwise
     */
    public boolean tryPlant(Cell player) {
        int bombsAllowed = 1 + arena.getTimeInSeconds() / BOMB_INCREMENT_INTERVAL;
        AtomicInteger bombsPlanted = bombMap.get(player.getId());
        if (bombsPlanted == null) {
            bombsPlanted = new AtomicInteger(0);
            bombMap.put(player.getId(), bombsPlanted);
        }
        if (bombsPlanted.get() < bombsAllowed) {
            BombRunnable bombRunnable = new BombRunnable(arena, this, player);
            Future<?> future = executorService.submit(bombRunnable);
            bombRunnable.setFuture(future);
            bombsPlanted.incrementAndGet();
            return true;
        }
        return false;
    }

    /**
     * Decrement number of currently planted by specified Player bombs
     *
     * @param playerId Player ID
     */
    public void decrementBombsUsed(String playerId) {
        bombMap.get(playerId).decrementAndGet();
    }

}
