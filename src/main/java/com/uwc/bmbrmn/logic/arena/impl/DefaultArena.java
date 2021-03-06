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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

/**
 * Default implementation of Arena interface
 */
@Component
@Scope(scopeName = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class DefaultArena implements Arena {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultArena.class);

    @Autowired
    private ChangesTracker changesTracker;

    @Autowired
    private BombManager bombManager;

    @Autowired
    private AIStrategy aiStrategy;

    @Value("${arena.width:13}")
    private int width;

    @Value("${arena.height:11}")
    private int height;

    private Table<Integer, Integer, Cell> arena;

    private Player player;
    private Collection<Bot> bots = new HashSet<>(3);

    private final AtomicInteger gameSecond = new AtomicInteger(0);

    /**
     * Init arena
     */
    @PostConstruct
    public void init() {
        arena = HashBasedTable.create(height, width);
        fillArena();
        initScheduledTasks();
    }

    /**
     * Init arena with cells
     */
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

    /**
     * Init scheduled tasks (e.g. TimeCounterRunnable, ResetPlayersStepsRunnable, BotActionRunnable)
     */
    private void initScheduledTasks() {
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(3);

        scheduledExecutorService.scheduleAtFixedRate(new TimeCounterRunnable(gameSecond), 0, BigInteger.ONE.intValue(), TimeUnit.SECONDS);
        scheduledExecutorService.scheduleAtFixedRate(new ResetPlayersStepsRunnable(player, bots), 0, RESET_PLAYERS_STEPS_DURATION, TimeUnit.SECONDS);
        scheduledExecutorService.scheduleAtFixedRate(new BotActionRunnable(aiStrategy, bots), HANDICAP_DELAY, BOT_ACTION_INTERVAL, TimeUnit.MILLISECONDS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getWidth() {
        return width;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getHeight() {
        return height;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Player getPlayer() {
        return player;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Bot> getBots() {
        return bots;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getTimeInSeconds() {
        return gameSecond.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Cell getCellAt(int x, int y) {
        return arena.get(y, x);
    }

    /**
     * {@inheritDoc}
     */
    private void putCellAt(int x, int y, Cell cell) {
        arena.put(y, x, cell);
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * Swap two cells
     *
     * @param item         First cell to swap
     * @param anotherCell  Second cell to swap
     * @param newPositionX x coordinate of second cell to swap
     * @param newPositionY y coordinate of second cell to swap
     */
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
            LOGGER.error(e.getMessage(), e);
        } finally {
            compositeLock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void plantBomb(Cell player) {
        try {
            if (player.getLock().tryLock(LOCK_TIMEOUT, TimeUnit.MILLISECONDS)) {
                if (bombManager.tryPlant(player)) {
                    player.setMined(true);
                }
            }
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            player.getLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void detonateBomb(int x, int y) {
        Collection<Cell> cellsToBurn = getCellsToBurn(x, y);
        CompositeLock compositeLock = getCompositeLock(cellsToBurn);
        try {
            if (compositeLock.tryLock(LOCK_TIMEOUT, TimeUnit.MILLISECONDS)) {
                cellsToBurn.forEach(this::burnCell);
            }
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            compositeLock.unlock();
        }
    }

    /**
     * Get cells which are going to burn if bomb detonated at specified location
     *
     * @param x x coordinate of bomb
     * @param y y coordinate of bomb
     * @return Cells going to burn
     */
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

    /**
     * Recursive method for filling collection of cells to burn
     *
     * @param cellsToBurn Collection of cells to burn
     * @param center      Mined cell
     * @param delta       Scan length
     * @param horizontal  Scan direction
     */
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

    /**
     * Burns specified cell
     *
     * @param cell Cell to burn
     */
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
