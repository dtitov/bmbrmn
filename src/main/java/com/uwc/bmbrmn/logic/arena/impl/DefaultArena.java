package com.uwc.bmbrmn.logic.arena.impl;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.uwc.bmbrmn.logic.ChangesTracker;
import com.uwc.bmbrmn.logic.ai.AIStrategy;
import com.uwc.bmbrmn.logic.ai.iml.BotActionRunnable;
import com.uwc.bmbrmn.logic.bombimg.BombManager;
import com.uwc.bmbrmn.logic.arena.Arena;
import com.uwc.bmbrmn.model.tiles.Cell;
import com.uwc.bmbrmn.model.tiles.impl.Block;
import com.uwc.bmbrmn.model.tiles.impl.Box;
import com.uwc.bmbrmn.model.tiles.impl.Space;
import com.uwc.bmbrmn.model.units.Bot;
import com.uwc.bmbrmn.model.units.Player;
import com.uwc.bmbrmn.scheduling.ResetPlayersStepsRunnable;
import com.uwc.bmbrmn.scheduling.TimeCounterRunnable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.PostConstruct;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Scope(scopeName = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class DefaultArena implements Arena {

    @Autowired
    private ChangesTracker<Cell> changesTracker;

    @Autowired
    private BombManager bombManager;

    @Autowired
    private AIStrategy aiStrategy;

    private int width;
    private int height;

    private Table<Integer, Integer, Cell> arena;

    private Player player;
    private Collection<Bot> bots = new HashSet<>(3);

    private final AtomicInteger gameSecond = new AtomicInteger(0);

    @PostConstruct
    public void init() {
        arena = HashBasedTable.create(height, width);
        fillArena();
        initScheduledTasks();
    }

    @Override
    public void fillArena() {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (isStartCell(i, j)) {
                    player = new Player(i, j);
                    arena.put(j, i, player);
                    continue;
                }
                if (isCornerCell(i, j)) {
                    Bot bot = new Bot(i, j);
                    bots.add(bot);
                    arena.put(j, i, bot);
                    continue;
                }
                if (isCriticalCell(i, j)) {
                    arena.put(j, i, new Space(i, j));
                    continue;
                }
                if (isUnevenCell(i, j)) {
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

    private void initScheduledTasks() {
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2 + bots.size());

        scheduledExecutorService.scheduleAtFixedRate(new TimeCounterRunnable(gameSecond), 0, BigInteger.ONE.intValue(), TimeUnit.SECONDS);
        scheduledExecutorService.scheduleAtFixedRate(new ResetPlayersStepsRunnable(player, bots), 0, RESET_PLAYERS_STEPS_DURATION, TimeUnit.SECONDS);

        for (Bot bot : bots) {
            scheduledExecutorService.scheduleAtFixedRate(new BotActionRunnable(aiStrategy, bot), HANDICAP_DELAY, BOT_ACTION_INTERVAL, TimeUnit.MILLISECONDS);
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
    public int getTimeInSeconds() {
        return gameSecond.get();
    }

    @Override
    public Cell getCellAt(int x, int y) {
        return arena.get(x, y);
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

    private void swapCells(Cell item, Cell anotherCell, int newPositionX, int newPositionY) {
        try {
            if (item.getLock().tryLock(LOCK_TIMEOUT, TimeUnit.MILLISECONDS)) {
                if (anotherCell.getLock().tryLock(LOCK_TIMEOUT, TimeUnit.MILLISECONDS)) {
                    arena.put(anotherCell.getY(), anotherCell.getX(), item);
                    arena.put(item.getY(), item.getX(), anotherCell);

                    anotherCell.move(item.getX(), item.getY());
                    item.move(newPositionX, newPositionY);

                    if (item.isMined()) {
                        item.setMined(false);
                        anotherCell.setMined(true);
                    }

                    anotherCell.setFlaming(false);

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
                if (bombManager.tryPlant(player)) {
                    player.setMined(true);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            player.getLock().unlock();
        }
    }

    @Override
    public void detonateBomb(int x, int y) {
        Collection<Cell> cellsToBurn = new HashSet<>();
        for (int i = -1; i <= 1; i++) {
            cellsToBurn.add(arena.get(y, x + i));
            cellsToBurn.add(arena.get(y + i, x));
        }
        cellsToBurn.removeAll(Collections.singleton((Cell) null));
        cellsToBurn.removeIf(c -> !c.isExplodable());
        try {
            for (Cell cell : cellsToBurn) {
                if (!cell.getLock().tryLock(LOCK_TIMEOUT, TimeUnit.MILLISECONDS)) {
                    return;
                }
            }
            for (Cell cell : cellsToBurn) {
                Cell newSpace;
                if (cell instanceof Space) {
                    newSpace = cell;
                    if (cell.getX() == x && cell.getY() == y) {
                        newSpace.setMined(false);
                    }
                } else {
                    newSpace = new Space(cell.getX(), cell.getY());
                    cell.move(-1, -1);
                    changesTracker.track(cell);
                }
                newSpace.setFlaming(true);
                arena.put(newSpace.getY(), newSpace.getX(), newSpace);
                changesTracker.track(newSpace);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            for (Cell cell : cellsToBurn) {
                cell.getLock().unlock();
            }
        }
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
