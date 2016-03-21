package com.uwc.bmbrmn.logic;

import com.uwc.bmbrmn.model.tiles.Cell;
import com.uwc.bmbrmn.model.units.Player;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Scope(scopeName = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ChangesTracker<T extends Cell> {

    private ConcurrentHashMap<String, T> changedItems = new ConcurrentHashMap<>();

    public void track(T cell) {
        changedItems.put(cell.getId(), cell);
    }

    public Collection<T> cutSlice() {
        Collection<T> slice = new ArrayList<>(changedItems.values());
        Collections.sort((List<T>) slice, (o1, o2) -> Boolean.compare(o1 instanceof Player, o2 instanceof Player));
        changedItems.clear();
        return slice;
    }

}
