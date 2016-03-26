package com.uwc.bmbrmn.logic.ai.iml;

import com.googlecode.concurentlocks.CompositeLock;
import com.uwc.bmbrmn.logic.Event;
import com.uwc.bmbrmn.logic.EventProcessor;
import com.uwc.bmbrmn.logic.ai.AIStrategy;
import com.uwc.bmbrmn.logic.arena.Arena;
import com.uwc.bmbrmn.model.tiles.Cell;
import com.uwc.bmbrmn.model.units.Bot;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.WebApplicationContext;
import org.xguzm.pathfinding.grid.GridCell;
import org.xguzm.pathfinding.grid.NavigationGrid;
import org.xguzm.pathfinding.grid.finders.AStarGridFinder;
import org.xguzm.pathfinding.grid.finders.GridFinderOptions;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.uwc.bmbrmn.logic.arena.Arena.LOCK_TIMEOUT;

@Service
@Scope(scopeName = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class DefaultAIStrategy implements AIStrategy {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultAIStrategy.class);

    @Autowired
    private Arena arena;

    @Autowired
    private EventProcessor eventProcessor;

    private AStarGridFinder<GridCell> finder;

    /**
     * Init AI
     */
    @PostConstruct
    public void init() {
        initPathfinder();
    }

    /**
     * Init pathfinder
     */
    private void initPathfinder() {
        GridFinderOptions options = new GridFinderOptions();
        options.allowDiagonal = false;
        finder = new AStarGridFinder<>(GridCell.class, options);
    }

    /**
     * Performs action with bot
     * Should get total lock for map to avoid race condition with Player's thread (input)
     * TODO: rework global locking because it causes lack of performance
     */
    @Override
    public void performAction(Bot bot) {
        CompositeLock mapLock = arena.getMapLock();
        try {
            if (mapLock.tryLock(LOCK_TIMEOUT, TimeUnit.MILLISECONDS)) {
                performActionInternally(bot);
            }
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            mapLock.unlock();
        }
    }

    /**
     * AI strategy:
     * - If there's no way to go - do nothing
     * - If Bot is in danger - try to take him to safe place
     * - If Bot is not in danger - place bomb
     * <p>
     * TODO: There should be check if planting bomb gonna kill the bot
     * TODO: If so, the bomb shouldn't be planted and the bot should go to some other cell
     *
     * @param bot Bot
     */
    private void performActionInternally(Bot bot) {
        if (CollectionUtils.isEmpty(getPossibleDirections(bot))) {
            return;
        }

        if (isInDanger(bot.toPair())) {
            goToSafety(bot);
        } else {
            eventProcessor.processEvent(Event.PLANT_BOMB, bot);
        }
    }

    /**
     * Finds path using A* algorithm
     *
     * @param from Start point
     * @param to   Destination point
     * @return Path represented as ordered sequence of points
     */
    private Collection<ImmutablePair<Integer, Integer>> findPath(Pair<Integer, Integer> from, Pair<Integer, Integer> to) {
        GridCell[][] gridCells = arena.toGridCellsArray();
        NavigationGrid<GridCell> navigationGrid = new NavigationGrid<>(gridCells, false);
        List<GridCell> path = finder.findPath(from.getLeft(), from.getRight(), to.getLeft(), to.getRight(), navigationGrid);
        if (CollectionUtils.isEmpty(path)) {
            return Collections.emptyList();
        }
        return path.stream().map(gridCell -> new ImmutablePair<>(gridCell.getX(), gridCell.getY())).collect(Collectors.toList());
    }

    /**
     * Makes one-cell step in one of four possible directions
     *
     * @param bot    Bot
     * @param target Destination point
     */
    private void stepTo(Bot bot, Pair<Integer, Integer> target) {
        Event event = null;
        if (target.getLeft() > bot.getX()) {
            event = Event.MOVE_RIGHT;
        } else if (target.getLeft() < bot.getX()) {
            event = Event.MOVE_LEFT;
        } else if (target.getRight() > bot.getY()) {
            event = Event.MOVE_DOWN;
        } else if (target.getRight() < bot.getY()) {
            event = Event.MOVE_UP;
        }
        eventProcessor.processEvent(event, bot);
    }

    /**
     * Calculates full path to destination point and performs first step
     *
     * @param bot    Bot
     * @param target Destination point
     */
    private void goTo(Bot bot, Pair<Integer, Integer> target) {
        Collection<ImmutablePair<Integer, Integer>> path = findPath(bot.toPair(), target);
        if (!CollectionUtils.isEmpty(path)) {
            stepTo(bot, path.iterator().next());
        }
    }

    /**
     * Gets all possible directions of for available: Up, Down, Left, Right
     *
     * @param bot Bot
     * @return Possible directions represented by events
     */
    private Collection<Event> getPossibleDirections(Bot bot) {
        Collection<Event> possibleDirections = new HashSet<>(4);

        Cell up = arena.getCellAt(bot.getX(), bot.getY() + 1);
        Cell down = arena.getCellAt(bot.getX(), bot.getY() - 1);
        Cell left = arena.getCellAt(bot.getX() - 1, bot.getY());
        Cell right = arena.getCellAt(bot.getX() + 1, bot.getY());

        if (up != null && up.isFree()) {
            possibleDirections.add(Event.MOVE_UP);
        }
        if (down != null && down.isFree()) {
            possibleDirections.add(Event.MOVE_DOWN);
        }
        if (left != null && left.isFree()) {
            possibleDirections.add(Event.MOVE_LEFT);
        }
        if (right != null && right.isFree()) {
            possibleDirections.add(Event.MOVE_RIGHT);
        }

        return possibleDirections;
    }

    /**
     * Tries to find safe place and go there if it exists
     *
     * @param bot Bot
     */
    private void goToSafety(Bot bot) {
        Pair<Integer, Integer> safePlace = findSafePlace(bot);
        if (safePlace != null) {
            goTo(bot, safePlace);
        }
    }

    /**
     * Tries to find safe place
     *
     * @param bot Bot
     * @return One of closest safe point or null if there's no safe point to reach
     */
    private Pair<Integer, Integer> findSafePlace(Bot bot) {
        for (int radius = 1; radius < Math.min(arena.getWidth(), arena.getHeight()) / 2; radius++) {
            for (int i = -radius; i <= radius; i++) {
                for (int j = -radius; j <= radius; j++) {
                    if (Math.abs(i) == radius || Math.abs(j) == radius) {
                        Cell cell = arena.getCellAt(bot.getX() + i, bot.getY() + j);
                        if (cell != null) {
                            Pair<Integer, Integer> pairCell = cell.toPair();
                            if (cell.isFree() && isSafe(pairCell) && isReachable(bot, pairCell)) {
                                return pairCell;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Checks if destination point is reachable
     *
     * @param bot    Bot
     * @param target Destination point
     * @return true if destination point is reachable, false otherwise
     */
    private boolean isReachable(Bot bot, Pair<Integer, Integer> target) {
        Collection<ImmutablePair<Integer, Integer>> path = findPath(bot.toPair(), target);
        return !CollectionUtils.isEmpty(path);
    }

    /**
     * Checks if point is safe
     *
     * @param cell Point to check
     * @return true if point is safe, false otherwise
     */
    private boolean isSafe(Pair<Integer, Integer> cell) {
        return CollectionUtils.isEmpty(getDangerousCells(cell));
    }

    /**
     * Checks if point is unsafe (might be detonated soon)
     *
     * @param cell Point to check
     * @return true if point is unsafe, false otherwise
     */
    private boolean isInDanger(Pair<Integer, Integer> cell) {
        return !isSafe(cell);
    }

    /**
     * Returns collection of dangerous (mined) points which will affect current point
     *
     * @param cell Point to check
     * @return Collection of dangerous (mined) points which will affect current poin
     */
    private Collection<Pair<Integer, Integer>> getDangerousCells(Pair<Integer, Integer> cell) {
        Collection<Pair<Integer, Integer>> dangerousCells = new HashSet<>(9);
        Cell currentCell = arena.getCellAt(cell.getLeft(), cell.getRight());
        if (currentCell.isMined()) {
            dangerousCells.add(cell);
        }
        collectCellsToBurn(dangerousCells, cell, -1, true);
        collectCellsToBurn(dangerousCells, cell, -1, false);
        collectCellsToBurn(dangerousCells, cell, 1, true);
        collectCellsToBurn(dangerousCells, cell, 1, false);
        return dangerousCells;
    }

    /**
     * Recursively fills collection of cells which are aoing to be burnt
     *
     * @param cellsToBurn Collection to fill
     * @param center      Center point
     * @param delta       Scanning length
     * @param horizontal  Scanning direction
     */
    private void collectCellsToBurn(Collection<Pair<Integer, Integer>> cellsToBurn, Pair<Integer, Integer> center, int delta, boolean horizontal) {
        if (Math.abs(delta) > Arena.BURNING_RADIUS + 1) {
            return;
        }
        int deltaX = horizontal ? delta : 0;
        int deltaY = !horizontal ? delta : 0;
        Cell candidate = arena.getCellAt(center.getLeft() + deltaX, center.getRight() + deltaY);
        if (candidate != null && candidate.isMined()) {
            cellsToBurn.add(candidate.toPair());
        } else {
            collectCellsToBurn(cellsToBurn, center, delta + (delta / Math.abs(delta)), horizontal);
        }
    }

}
