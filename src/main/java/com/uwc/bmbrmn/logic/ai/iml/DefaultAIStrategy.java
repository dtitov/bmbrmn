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
import java.util.concurrent.ThreadLocalRandom;
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

    @PostConstruct
    public void init() {
        initPathfinder();
    }

    private void initPathfinder() {
        GridFinderOptions options = new GridFinderOptions();
        options.allowDiagonal = false;
        finder = new AStarGridFinder<>(GridCell.class, options);
    }

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

    private void performActionInternally(Bot bot) {
        if (CollectionUtils.isEmpty(getPossibleDirections(bot))) {
            return;
        }

        if (isInDanger(bot.toPair())) {
            goToSafety(bot);
            return;
        }

        ThreadLocalRandom current = ThreadLocalRandom.current();
        boolean plantBomb = current.nextBoolean();
        if (plantBomb) {
            eventProcessor.processEvent(Event.PLANT_BOMB, bot);
        }
    }

    private Collection<ImmutablePair<Integer, Integer>> findPath(Pair<Integer, Integer> from, Pair<Integer, Integer> to) {
        GridCell[][] gridCells = arena.toGridCellsArray();
        NavigationGrid<GridCell> navigationGrid = new NavigationGrid<>(gridCells, false);
        List<GridCell> path = finder.findPath(from.getLeft(), from.getRight(), to.getLeft(), to.getRight(), navigationGrid);
        if (CollectionUtils.isEmpty(path)) {
            return Collections.emptyList();
        }
        return path.stream().map(gridCell -> new ImmutablePair<>(gridCell.getX(), gridCell.getY())).collect(Collectors.toList());
    }

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

    private void goTo(Bot bot, Pair<Integer, Integer> target) {
        Collection<ImmutablePair<Integer, Integer>> path = findPath(bot.toPair(), target);
        if (!CollectionUtils.isEmpty(path)) {
            stepTo(bot, path.iterator().next());
        }
    }

    private Collection<Event> getPossibleDirections(Bot bot) {
        Collection<Event> possibleDirections = new HashSet<>(4);

        Cell up = arena.getCellAt(bot.getX(), bot.getY() + 1);
        Cell down = arena.getCellAt(bot.getX(), bot.getY() - 1);
        Cell left = arena.getCellAt(bot.getX() - 1, bot.getY());
        Cell right = arena.getCellAt(bot.getX() + 1, bot.getY());

        if (up != null && up.isFree() && isSafe(up.toPair())) {
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

    private void goToSafety(Bot bot) {
        Pair<Integer, Integer> safePlace = findSafePlace(bot);
        if (safePlace != null) {
            goTo(bot, safePlace);
        }
    }

    private Pair<Integer, Integer> findSafePlace(Bot bot) {
        for (int i = -arena.getWidth() / 2; i < arena.getWidth() / 2; i++) {
            for (int j = -arena.getHeight() / 2; j < arena.getHeight() / 2; j++) {
                Cell cell = arena.getCellAt(bot.getX() + i, bot.getY() + j);
                if (cell != null) {
                    Pair<Integer, Integer> pairCell = cell.toPair();
                    if (cell.isFree() && isSafe(pairCell) && isReachable(bot, pairCell)) {
                        return pairCell;
                    }
                }
            }
        }
        return null;
    }

    private boolean isReachable(Bot bot, Pair<Integer, Integer> target) {
        Collection<ImmutablePair<Integer, Integer>> path = findPath(bot.toPair(), target);
        return !CollectionUtils.isEmpty(path);
    }

    private boolean isSafe(Pair<Integer, Integer> cell) {
        return CollectionUtils.isEmpty(getDangerousCells(cell));
    }

    private boolean isInDanger(Pair<Integer, Integer> cell) {
        return !isSafe(cell);
    }

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
