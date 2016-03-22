package com.uwc.bmbrmn.logic.ai.iml;

import com.uwc.bmbrmn.logic.Event;
import com.uwc.bmbrmn.logic.EventProcessor;
import com.uwc.bmbrmn.logic.ai.AIStrategy;
import com.uwc.bmbrmn.logic.arena.Arena;
import com.uwc.bmbrmn.model.units.Bot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

import java.util.concurrent.ThreadLocalRandom;

@Service
@Scope(scopeName = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class DefaultAIStrategy implements AIStrategy {

    @Autowired
    private Arena arena;

    @Autowired
    private EventProcessor eventProcessor;

    @Override
    public void performAction(Bot bot) {
        ThreadLocalRandom current = ThreadLocalRandom.current();
        boolean plantBomb = current.nextBoolean();
        if (plantBomb) {
            eventProcessor.processEvent(Event.PLANT_BOMB, bot);
        }
        double direction = current.nextDouble(1);
        if (direction < 0.25) {
            eventProcessor.processEvent(Event.MOVE_LEFT, bot);
        } else if (direction >= 0.25 && direction < 0.5) {
            eventProcessor.processEvent(Event.MOVE_RIGHT, bot);
        } else if (direction >= 0.5 && direction < 0.75) {
            eventProcessor.processEvent(Event.MOVE_UP, bot);
        } else if (direction >= 0.75) {
            eventProcessor.processEvent(Event.MOVE_DOWN, bot);
        }
    }

}
