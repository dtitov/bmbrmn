package com.uwc.bmbrmn.logic.arena.impl;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.googlecode.concurentlocks.CompositeLock;
import com.uwc.bmbrmn.logic.ChangesTracker;
import com.uwc.bmbrmn.logic.ai.AIStrategy;
import com.uwc.bmbrmn.logic.ai.iml.BotActionRunnable;
import com.uwc.bmbrmn.logic.arena.Arena;
import com.uwc.bmbrmn.logic.bombimg.BombManager;
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
                    putCellAt(i, j, player);
                    continue;
                }
                if (isCornerCell(i, j)) {
                    Bot bot = new Bot(i, j);
                    bots.add(bot);
                    putCellAt(i, j, bot);
                    continue;
                }
                if (isCriticalCell(i, j)) {
                    putCellAt(i, j, new Space(i, j));
                    continue;
                }
                if (isUnevenCell(i, j)) {
                    putCellAt(i, j, new Block(i, j));
                    continue;
                }
                if (ThreadLocalRandom.current().nextFloat() > BOX_THRESHOLD) {
                    putCellAt(i, j, new Box(i, j));
                    continue;
                }
                putCellAt(i, j, new Space(i, j));
            }
        }
    }

    private void initScheduledTasks() {
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(3);

        scheduledExecutorService.scheduleAtFixedRate(new TimeCounterRunnable(gameSecond), 0, BigInteger.ONE.intValue(), TimeUnit.SECONDS);
        scheduledExecutorService.scheduleAtFixedRate(new ResetPlayersStepsRunnable(player, bots), 0, RESET_PLAYERS_STEPS_DURATION, TimeUnit.SECONDS);
        scheduledExecutorService.scheduleAtFixedRate(new BotActionRunnable(aiStrategy, bots), HANDICAP_DELAY, BOT_ACTION_INTERVAL, TimeUnit.MILLISECONDS);
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
    public Collection<Bot> getBots() {
        return bots;
    }

    @Override
    public int getTimeInSeconds() {
        return gameSecond.get();
    }

    @Override
    public Cell getCellAt(int x, int y) {
        return arena.get(y, x);
    }

    private void putCellAt(int x, int y, Cell cell) {
        arena.put(y, x, cell);
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
        Cell anotherCell = getCellAt(newPositionX, newPositionY);
        if (anotherCell == null || !anotherCell.isFree()) {
            return;
        }

        swapCells(item, anotherCell, newPositionX, newPositionY);
    }

    private void swapCells(Cell item, Cell anotherCell, int newPositionX, int newPositionY) {
        CompositeLock compositeLock = getCompositeLock(item, anotherCell);
        try {
            if (compositeLock.tryLock(LOCK_TIMEOUT, TimeUnit.MILLISECONDS)) {
                putCellAt(anotherCell.getX(), anotherCell.getY(), item);
                putCellAt(item.getX(), item.getY(), anotherCell);

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
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            compositeLock.unlock();
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
        Collection<Cell> cellsToBurn = getCellsToBurn(x, y);
        CompositeLock compositeLock = getCompositeLock(cellsToBurn);
        try {
            if (compositeLock.tryLock(LOCK_TIMEOUT, TimeUnit.MILLISECONDS)) {
                cellsToBurn.forEach(this::burnCell);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            compositeLock.unlock();
        }
    }

    private Collection<Cell> getCellsToBurn(int x, int y) {
        Collection<Cell> cellsToBurn = new HashSet<>(9);
        Cell center = getCellAt(x, y);
        center.setMined(false);
        cellsToBurn.add(center);
        collectCellsToBurn(cellsToBurn, center, -1, true);
        collectCellsToBurn(cellsToBurn, center, -1, false);
        collectCellsToBurn(cellsToBurn, center, 1, true);
        collectCellsToBurn(cellsToBurn, center, 1, false);
        return cellsToBurn;
    }

    private void collectCellsToBurn(Collection<Cell> cellsToBurn, Cell center, int delta, boolean horizontal) {
        if (Math.abs(delta) > BURNING_RADIUS) {
            return;
        }
        int deltaX = horizontal ? delta : 0;
        int deltaY = !horizontal ? delta : 0;
        Cell candidate = getCellAt(center.getX() + deltaX, center.getY() + deltaY);
        if (candidate != null && candidate.isExplodable()) {
            cellsToBurn.add(candidate);
            collectCellsToBurn(cellsToBurn, center, delta + (delta / Math.abs(delta)), horizontal);
        }
    }

    private void burnCell(Cell cell) {
        Cell newSpace;
        if (cell instanceof Space) {
            newSpace = cell;
        } else {
            newSpace = new Space(cell.getX(), cell.getY());
            cell.move(-1, -1);
            if (cell instanceof Player) {
                ((Player) cell).setAlive(false);
            }
            changesTracker.track(cell);
        }
        newSpace.setFlaming(true);
        putCellAt(newSpace.getX(), newSpace.getY(), newSpace);
        changesTracker.track(newSpace);
    }

}
