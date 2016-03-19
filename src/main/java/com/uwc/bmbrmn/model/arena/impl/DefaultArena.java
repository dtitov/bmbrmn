package com.uwc.bmbrmn.model.arena.impl;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.uwc.bmbrmn.model.arena.Arena;
import com.uwc.bmbrmn.model.arena.Cell;
import com.uwc.bmbrmn.model.arena.Navigable;
import com.uwc.bmbrmn.model.units.Bomb;
import com.uwc.bmbrmn.model.units.Bot;
import com.uwc.bmbrmn.model.units.Player;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.PostConstruct;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Component
@Scope(scopeName = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class DefaultArena implements Arena {

    private int width;
    private int height;

    private Table<Integer, Integer, Cell> arena;

    private Player player;

    @PostConstruct
    public void init() {
        arena = HashBasedTable.create(height, width);
        fillArena();
    }

    @Override
    public void fillArena() {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (isStart(i, j)) {
                    player = new Player(i, j);
                    arena.put(j, i, player);
                    continue;
                }
                if (isCorner(i, j)) {
                    arena.put(j, i, new Bot(i, j));
                    continue;
                }
                if (isBlock(i, j)) {
                    arena.put(j, i, new Block());
                    continue;
                }
                if (ThreadLocalRandom.current().nextFloat() > BOX_THRESHOLD) {
                    arena.put(j, i, new Box());
                    continue;
                }
                arena.put(j, i, new Space());
            }
        }
    }


    @Override
    public int getWidth() {
        return width;
    }

    @Value("${arena.width:13}")
    public void setWidth(int width) {
        this.width = width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Value("${arena.height:11}")
    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public void moveItem(Navigable item, int deltaX, int deltaY) {
        if (!item.isMovable()) {
            return;
        }
        if (Math.abs(deltaX) > 1 || Math.abs(deltaY) > 1) {
            return;
        }

        int newPositionX = item.getX() + deltaX;
        int newPositionY = item.getY() + deltaY;
        Cell newPosition = arena.get(newPositionY, newPositionX);
        if (newPosition == null || !newPosition.isFree()) {
            return;
        }

        try {
            if (item.getLock().tryLock(LOCK_TIMEOUT, TimeUnit.MILLISECONDS)) {
                if (newPosition.getLock().tryLock(LOCK_TIMEOUT, TimeUnit.MILLISECONDS)) {
                    arena.put(newPositionY, newPositionX, item);
                    arena.put(item.getY(), item.getX(), newPosition);
                    item.setX(newPositionX);
                    item.setY(newPositionY);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            item.getLock().unlock();
            newPosition.getLock().unlock();
        }
    }

    @Override
    public void plantBomb(Navigable player) {
        try {
            if (player.getLock().tryLock(LOCK_TIMEOUT, TimeUnit.MILLISECONDS)) {
                arena.put(player.getY(), player.getX(), new Bomb(player.getX(), player.getY()));
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            player.getLock().unlock();
        }
    }

    @Override
    public void detonateBomb(Navigable item) {

    }

    @Override
    public String[][] toArray() {
        String[][] cells = new String[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                cells[i][j] = arena.get(j, i).getClass().getSimpleName();
            }
        }
        return cells;
    }

}
