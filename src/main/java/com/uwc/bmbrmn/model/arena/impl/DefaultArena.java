package com.uwc.bmbrmn.model.arena.impl;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.uwc.bmbrmn.model.arena.Arena;
import com.uwc.bmbrmn.model.arena.Cell;
import com.uwc.bmbrmn.model.units.Player;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.PostConstruct;
import java.util.concurrent.ThreadLocalRandom;

@Component
@Scope(scopeName = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class DefaultArena implements Arena {

    private int width;
    private int height;

    private Table<Integer, Integer, Cell> arena;

    @PostConstruct
    public void init() {
        arena = HashBasedTable.create(height, width);
        fillArena();
    }

    public void fillArena() {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (isStart(i, j)) {
                    arena.put(j, i, new Player());
                    continue;
                }
                if (isCorner(i, j)) {
                    arena.put(j, i, new Player());
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
    public String[][] toArray() {
        String[][] cells = new String[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                cells[i][j] = arena.get(j, i).getClass().getSimpleName();
            }
        }
        return cells;
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
}
