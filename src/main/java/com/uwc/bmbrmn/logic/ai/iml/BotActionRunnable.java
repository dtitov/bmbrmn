package com.uwc.bmbrmn.logic.ai.iml;

import com.uwc.bmbrmn.logic.ai.AIStrategy;
import com.uwc.bmbrmn.model.units.Bot;
import com.uwc.bmbrmn.scheduling.RequestAwareRunnable;

public class BotActionRunnable extends RequestAwareRunnable {

    private AIStrategy aiStrategy;
    private Bot bot;

    public BotActionRunnable(AIStrategy aiStrategy, Bot bot) {
        this.aiStrategy = aiStrategy;
        this.bot = bot;
    }

    @Override
    protected void onRun() {
        if (bot.isAlive()) {
            aiStrategy.performAction(bot);
        }
    }

}
