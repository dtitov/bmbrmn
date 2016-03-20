package com.uwc.bmbrmn.logic.bombimg;

import com.uwc.bmbrmn.model.arena.Arena;
import com.uwc.bmbrmn.model.arena.Cell;
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
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Scope(scopeName = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class BombManager {

    public static final int BOMB_INCREMENT_INTERVAL = 30;
    public static final int DETONATION_DELAY = 3000;

    @Autowired
    protected Arena arena;

    private Map<String, AtomicInteger> bombMap = new ConcurrentHashMap<>(8);

    private ExecutorService executorService;

    @PostConstruct
    public void init() {
        executorService = Executors.newFixedThreadPool(arena.getWidth() * arena.getHeight());
    }

    public boolean tryPlant(Cell player) {
        int bombsAllowed = 1 + arena.getSecond() / BOMB_INCREMENT_INTERVAL;
        AtomicInteger bombsPlanted = bombMap.get(player.getId());
        if (bombsPlanted == null) {
            bombsPlanted = new AtomicInteger(0);
            bombMap.put(player.getId(), bombsPlanted);
        }
        if (bombsPlanted.get() < bombsAllowed) {
            BombRunnable bombRunnable = new BombRunnable(arena, this, player);
            executorService.execute(bombRunnable);
            bombsPlanted.incrementAndGet();
            return true;
        }
        return false;
    }

    public void decrementBombsUsed(String playerId) {
        bombMap.get(playerId).decrementAndGet();
    }

}
