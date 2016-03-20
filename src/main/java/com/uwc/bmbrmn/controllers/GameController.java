package com.uwc.bmbrmn.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uwc.bmbrmn.logic.ChangesTracker;
import com.uwc.bmbrmn.logic.Event;
import com.uwc.bmbrmn.logic.EventProcessor;
import com.uwc.bmbrmn.model.arena.Arena;
import com.uwc.bmbrmn.model.arena.Cell;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class GameController {

    @Autowired
    private Arena arena;

    @Autowired
    private EventProcessor eventProcessor;

    @Autowired
    private ChangesTracker<Cell> changesTracker;

    @RequestMapping(value = "/getArena", produces = "application/json")
    @ResponseBody
    public String[][] getArena() {
        return arena.toArray();
    }

    @RequestMapping(value = "/updateStatus", produces = "text/event-stream")
    @ResponseBody
    public String updateStatus() {
        ObjectMapper mapper = new ObjectMapper();
        String stringArena = null;
        try {
            stringArena = mapper.writeValueAsString(changesTracker.cutSlice());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return "retry: 100\ndata: " + stringArena + "\n\n";
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

}
