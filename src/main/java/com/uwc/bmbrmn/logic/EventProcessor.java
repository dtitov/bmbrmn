package com.uwc.bmbrmn.logic;

import com.uwc.bmbrmn.model.arena.Arena;
import com.uwc.bmbrmn.model.arena.Cell;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

@Service
@Scope(scopeName = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class EventProcessor {

    @Autowired
    private Arena arena;

    public void processEvent(Event event, Cell item) {
        switch (event) {
            case MOVE_DOWN:
                arena.moveItem(item, 0, 1);
                break;
            case MOVE_UP:
                arena.moveItem(item, 0, -1);
                break;
            case MOVE_LEFT:
                arena.moveItem(item, -1, 0);
                break;
            case MOVE_RIGHT:
                arena.moveItem(item, 1, 0);
                break;
            case PLANT_BOMB:
                arena.plantBomb(item);
                break;
            case DETONATE_BOMB:
                arena.detonateBomb(item);
                break;
        }
    }

}
