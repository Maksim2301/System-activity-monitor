package com.example.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;


public class MonitoringThreadFactory implements ThreadFactory {

    private final String prefix;
    private final AtomicInteger count = new AtomicInteger(1);

    public MonitoringThreadFactory(String prefix) {
        this.prefix = prefix == null ? "monitor" : prefix;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(() -> {
            try {
                r.run();
            } catch (Throwable e) {
                System.err.println("Uncaught exception in monitored thread: " + e.getMessage());
                e.printStackTrace();
            }
        });

        t.setName(prefix + "-thread-" + count.getAndIncrement());
        t.setDaemon(true);

        return t;
    }
}
