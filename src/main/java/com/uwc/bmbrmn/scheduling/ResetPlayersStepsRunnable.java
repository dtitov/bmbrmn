package com.uwc.bmbrmn.scheduling;

import com.uwc.bmbrmn.model.units.Bot;
import com.uwc.bmbrmn.model.units.Player;

import java.util.Collection;

public class ResetPlayersStepsRunnable implements Runnable {

    private Player player;
    private Collection<Bot> bots;

    public ResetPlayersStepsRunnable(Player player, Collection<Bot> bots) {
        this.player = player;
        this.bots = bots;
    }

    @Override
    public void run() {
        player.resetSteps();
        bots.forEach(Bot::resetSteps);
    }

}
