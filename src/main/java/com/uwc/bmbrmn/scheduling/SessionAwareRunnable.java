package com.uwc.bmbrmn.scheduling;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * Runnable aware of HTTP session
 */
public abstract class SessionAwareRunnable implements Runnable {

    private final RequestAttributes requestAttributes;
    private Thread thread;

    public SessionAwareRunnable() {
        this.requestAttributes = RequestContextHolder.getRequestAttributes();
        this.thread = Thread.currentThread();
    }

    public void run() {
        try {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            onRun();
        } finally {
            if (Thread.currentThread() != thread) {
                RequestContextHolder.resetRequestAttributes();
            }
            thread = null;
        }
    }

    protected abstract void onRun();

}