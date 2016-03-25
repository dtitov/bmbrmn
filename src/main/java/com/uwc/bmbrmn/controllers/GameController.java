package com.uwc.bmbrmn.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uwc.bmbrmn.logic.ChangesTracker;
import com.uwc.bmbrmn.logic.Event;
import com.uwc.bmbrmn.logic.EventProcessor;
import com.uwc.bmbrmn.logic.arena.Arena;
import com.uwc.bmbrmn.model.tiles.Cell;
import com.uwc.bmbrmn.model.units.Bot;
import com.uwc.bmbrmn.model.units.Player;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.math.BigInteger;
import java.util.Collection;

@RestController
@RequestMapping("/")
public class GameController {

    public static final String GAME_OVER = "GAME_OVER";

    @Autowired
    private Arena arena;

    @Autowired
    private EventProcessor eventProcessor;

    @Autowired
    private ChangesTracker<Cell> changesTracker;

    @RequestMapping(value = "/getArena", produces = "application/json")
    @ResponseBody
    public String[][] getArena() {
        return arena.toStringArray();
    }

    @RequestMapping(value = "/updateStatus", produces = "text/event-stream")
    @ResponseBody
    public String updateStatus() {
        ObjectMapper mapper = new ObjectMapper();
        String status = null;
        try {
            Collection<Cell> slice = changesTracker.cutSlice();
            if (isGameOver()) {
                slice.add(new Player(0, 0) {
                    @Override
                    public String getId() {
                        return GAME_OVER;
                    }
                });
            }
            status = mapper.writeValueAsString(slice);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return "retry: 100\ndata: " + status + "\n\n";
    }

    private boolean isGameOver() {
        int aliveUnits = arena.getPlayer().isAlive() ? 1 : 0;
        for (Bot bot : arena.getBots()) {
            if (bot.isAlive()) {
                aliveUnits++;
            }
        }
        return aliveUnits == BigInteger.ONE.intValue();
    }

    @RequestMapping("/moveUp")
    public void moveUp() {
        eventProcessor.processEvent(Event.MOVE_UP, arena.getPlayer());
    }

    @RequestMapping("/moveDown")
    public void moveDown() {
        eventProcessor.processEvent(Event.MOVE_DOWN, arena.getPlayer());
    }

    @RequestMapping("/moveLeft")
    public void moveLeft() {
        eventProcessor.processEvent(Event.MOVE_LEFT, arena.getPlayer());
    }

    @RequestMapping("/moveRight")
    public void moveRight() {
        eventProcessor.processEvent(Event.MOVE_RIGHT, arena.getPlayer());
    }

    @RequestMapping("/plantBomb")
    public void plantBomb() {
        eventProcessor.processEvent(Event.PLANT_BOMB, arena.getPlayer());
    }

    @RequestMapping("/newGame")
    public String newGame(HttpSession session) {
        session.invalidate();
        return HttpStatus.OK.toString();
    }

}
