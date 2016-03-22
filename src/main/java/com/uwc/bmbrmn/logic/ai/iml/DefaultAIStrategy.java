package com.uwc.bmbrmn.logic.ai.iml;

import com.uwc.bmbrmn.logic.EventProcessor;
import com.uwc.bmbrmn.logic.ai.AIStrategy;
import com.uwc.bmbrmn.model.units.Bot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

@Service
@Scope(scopeName = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class DefaultAIStrategy implements AIStrategy {

    @Autowired
    private EventProcessor eventProcessor;

    @Override
    public void performAction(Bot bot) {

    }

}
