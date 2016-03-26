package com.uwc.bmbrmn.logic.ai;

import com.uwc.bmbrmn.model.units.Bot;

/**
 * Bots behavior
 */
public interface AIStrategy {

    /**
     * Makes decision about bot's action
     *
     * @param bot Bot
     */
    void performAction(Bot bot);

}
