package com.uwc.bmbrmn.model.arena.impl;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.uwc.bmbrmn.logic.ChangesTracker;
import com.uwc.bmbrmn.model.arena.Arena;
import com.uwc.bmbrmn.model.arena.Cell;
import com.uwc.bmbrmn.model.units.Bot;
import com.uwc.bmbrmn.model.units.Player;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private ChangesTracker<Cell> changesTracker;

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
                if (isCriticalPoint(i, j)) {
                    arena.put(j, i, new Space(i, j));
                    continue;
                }
                if (isBlock(i, j)) {
                    arena.put(j, i, new Block(i, j));
                    continue;
                }
                if (ThreadLocalRandom.current().nextFloat() > BOX_THRESHOLD) {
                    arena.put(j, i, new Box(i, j));
                    continue;
                }
                arena.put(j, i, new Space(i, j));
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
    public void moveItem(Cell item, int deltaX, int deltaY) {
        if (!item.isMovable()) {
            return;
        }
        if (Math.abs(deltaX) > 1 || Math.abs(deltaY) > 1) {
            return;
        }

        int newPositionX = item.getX() + deltaX;
        int newPositionY = item.getY() + deltaY;
        Cell anotherCell = arena.get(newPositionY, newPositionX);
        if (anotherCell == null || !anotherCell.isFree()) {
            return;
        }

        swapCells(item, anotherCell, newPositionX, newPositionY);
    }

    protected void swapCells(Cell item, Cell anotherCell, int newPositionX, int newPositionY) {
        try {
            if (item.getLock().tryLock(LOCK_TIMEOUT, TimeUnit.MILLISECONDS)) {
                if (anotherCell.getLock().tryLock(LOCK_TIMEOUT, TimeUnit.MILLISECONDS)) {
                    arena.put(anotherCell.getY(), anotherCell.getX(), item);
                    arena.put(item.getY(), item.getX(), anotherCell);

                    anotherCell.setX(item.getX());
                    anotherCell.setY(item.getY());
                    item.setX(newPositionX);
                    item.setY(newPositionY);

                    if (item.isMined()) {
                        item.setMined(false);
                        anotherCell.setMined(true);
                    }

                    changesTracker.track(item);
                    changesTracker.track(anotherCell);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            item.getLock().unlock();
            anotherCell.getLock().unlock();
        }
    }

    @Override
    public void plantBomb(Cell player) {
        try {
            if (player.getLock().tryLock(LOCK_TIMEOUT, TimeUnit.MILLISECONDS)) {
                player.setMined(true);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            player.getLock().unlock();
        }
    }

    @Override
    public void detonateBomb(Cell item) {

    }

    @Override
    public String[][] toArray() {
        String[][] cells = new String[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                Cell cell = arena.get(j, i);
                cells[i][j] = cell.getId() + ":" + cell.getClass().getSimpleName();
            }
        }
        return cells;
    }

}
