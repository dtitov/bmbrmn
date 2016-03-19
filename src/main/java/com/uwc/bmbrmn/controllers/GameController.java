package com.uwc.bmbrmn.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uwc.bmbrmn.logic.Event;
import com.uwc.bmbrmn.logic.EventProcessor;
import com.uwc.bmbrmn.model.arena.Arena;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
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

    @RequestMapping(value = "/getArena", produces = "application/json")
    @ResponseBody
    public String[][] getArena() {
        return arena.toArray();
    }

    @RequestMapping(value = "/updateArena", produces = "text/event-stream")
    @ResponseBody
    @Scheduled(initialDelay = 1000, fixedRate = 1000)
    public String updateArena() {
        ObjectMapper mapper = new ObjectMapper();
        String stringArena = null;
        try {
            stringArena = mapper.writeValueAsString(arena.toArray());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return "data: " + stringArena + "\n\n";
    }

    @RequestMapping("/moveUp")
    public void moveUp() {
        eventProcessor.processEvent(Event.MOVE_UP);
    }

    @RequestMapping("/moveDown")
    public void moveDown() {
        eventProcessor.processEvent(Event.MOVE_DOWN);
    }

    @RequestMapping("/moveLeft")
    public void moveLeft() {
        eventProcessor.processEvent(Event.MOVE_LEFT);
    }

    @RequestMapping("/moveRight")
    public void moveRight() {
        eventProcessor.processEvent(Event.MOVE_RIGHT);
    }

    @RequestMapping("/plantBomb")
    public void plantBomb() {
        eventProcessor.processEvent(Event.PLANT);
    }

}
