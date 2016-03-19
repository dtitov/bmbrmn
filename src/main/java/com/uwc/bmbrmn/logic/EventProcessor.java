package com.uwc.bmbrmn.logic;

import com.uwc.bmbrmn.controllers.GameController;
import com.uwc.bmbrmn.model.arena.Arena;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

@Service
@Scope(scopeName = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class EventProcessor {

    @Autowired
    private GameController gameController;

    @Autowired
    private Arena arena;

    public void processEvent(Event event) {
        processEventInternal(event);
        gameController.updateArena();
    }

    protected void processEventInternal(Event event) {

    }

}
