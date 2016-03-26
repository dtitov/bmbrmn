package com.uwc.bmbrmn.logic.ai.iml;

import com.uwc.bmbrmn.logic.ai.AIStrategy;
import com.uwc.bmbrmn.model.units.Bot;
import com.uwc.bmbrmn.scheduling.RequestAwareRunnable;

import java.util.Collection;

public class BotActionRunnable extends RequestAwareRunnable {

    private AIStrategy aiStrategy;
    private Collection<Bot> bots;

    public BotActionRunnable(AIStrategy aiStrategy, Collection<Bot> bots) {
        this.aiStrategy = aiStrategy;
        this.bots = bots;
    }

    @Override
    protected void onRun() {
        bots.stream().filter(Bot::isAlive).forEach(bot -> aiStrategy.performAction(bot));
    }

}
