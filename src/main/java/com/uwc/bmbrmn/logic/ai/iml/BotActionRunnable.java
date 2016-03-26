package com.uwc.bmbrmn.logic.ai.iml;

import com.uwc.bmbrmn.logic.ai.AIStrategy;
import com.uwc.bmbrmn.model.units.Bot;
import com.uwc.bmbrmn.scheduling.SessionAwareRunnable;

import java.util.Collection;

/**
 * Scheduled runnable for making periodical calls to AIStrategy for performing bots' actions
 */
public class BotActionRunnable extends SessionAwareRunnable {

    private AIStrategy aiStrategy;
    private Collection<Bot> bots;

    public BotActionRunnable(AIStrategy aiStrategy, Collection<Bot> bots) {
        this.aiStrategy = aiStrategy;
        this.bots = bots;
    }

    /**
     * Filters out alive bots and calls AIStrategy for them
     */
    @Override
    protected void onRun() {
        bots.stream().filter(Bot::isAlive).forEach(bot -> aiStrategy.performAction(bot));
    }

}
