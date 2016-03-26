package com.uwc.bmbrmn.logic;

import com.uwc.bmbrmn.model.tiles.Cell;
import com.uwc.bmbrmn.model.units.Player;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for tracking all changes happened to Cells
 */
@Service
@Scope(scopeName = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ChangesTracker {

    private ConcurrentHashMap<String, Cell> changedItems = new ConcurrentHashMap<>();

    /**
     * Track Cell affected by any change
     *
     * @param cell Affected Cell
     */
    public void track(Cell cell) {
        changedItems.put(cell.getId(), cell);
    }

    /**
     * Returns patch of tracked Cells
     *
     * @return Collection of changed since last query Cells
     */
    public Collection<Cell> cutSlice() {
        Collection<Cell> slice = new ArrayList<>(changedItems.values());
        Collections.sort((List<Cell>) slice, (o1, o2) -> Boolean.compare(o1 instanceof Player, o2 instanceof Player));
        changedItems.clear();
        return slice;
    }

}
