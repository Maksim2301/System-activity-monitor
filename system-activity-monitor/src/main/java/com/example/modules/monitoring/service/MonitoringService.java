package com.example.modules.monitoring.service;

import com.example.modules.monitoring.metrics.MetricsProvider;
import com.example.model.SystemStats;
import com.example.model.User;
import com.example.repository.impl.StatsRepositoryImpl;
import com.example.repository.interfaces.StatsRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.util.Map;
import java.util.concurrent.*;

public class MonitoringService {

    protected final StatsRepository statsRepository = new StatsRepositoryImpl();
    protected final MetricsProvider metricsProvider;

    protected ScheduledExecutorService scheduler;
    protected volatile boolean active = false;
    protected User activeUser;

    private final ThreadFactory threadFactory = runnable -> {
        Thread t = new Thread(runnable);
        t.setName("MonitoringScheduler-" + t.getId());
        t.setDaemon(true);
        t.setUncaughtExceptionHandler((thr, ex) ->
                System.err.println("Uncaught exception in " + thr.getName() + ": " + ex)
        );
        return t;
    };

    public MonitoringService(MetricsProvider provider) {
        this.metricsProvider = provider;
    }

    public synchronized void start(User user) {
        if (active) return;

        active = true;
        this.activeUser = user;

        scheduler = Executors.newScheduledThreadPool(2, threadFactory);

        // 🔹 Швидке оновлення UI / метрик кожні 5 секунд
        scheduler.scheduleAtFixedRate(() -> safeGuard(this::tickFast),
                0, 5, TimeUnit.SECONDS);

        // 🔹 Повільний запис у базу кожну 1 хвилину
        scheduler.scheduleAtFixedRate(() -> safeGuard(this::tickSlow),
                0, 1, TimeUnit.MINUTES);

        metricsProvider.startInputMonitoring();

        System.out.println("MonitoringService: monitoring is started.");
    }


    public synchronized void stop() {
        active = false;

        metricsProvider.stopInputMonitoring();

        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }

        System.out.println("Monitoring stopped.");
    }

    protected void safeGuard(Runnable task) {
        try {
            task.run();
        } catch (Exception e) {
            System.err.println("Exception in scheduled task: " + e.getMessage());
        }
    }
    protected void collectMetrics() {
        if (!active) return;

        Map<String, Object> data = metricsProvider.collectAllMetrics();
        logWarnings(data);
        if (activeUser != null) {
            recordSystemStats(data, activeUser);
        }
    }

    public Map<String, Object> collectFormattedStats() {
        return metricsProvider.collectAllMetrics();
    }

    private void logWarnings(Map<String, Object> data) {

        BigDecimal cpu = toDecimal(data.get("cpuLoad"));
        BigDecimal ramUsed = toDecimal(data.get("ramUsed"));
        BigDecimal ramTotal = toDecimal(data.get("ramTotal"));
        BigDecimal diskTotal = toDecimal(data.get("diskTotal"));
        BigDecimal diskFree = toDecimal(data.get("diskFree"));

        // CPU warning
        if (cpu.compareTo(BigDecimal.valueOf(90)) > 0) {
            System.out.printf("High CPU load (%.2f%%)%n", cpu);
        }

        // RAM warning
        if (ramTotal.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal percent = ramUsed.divide(ramTotal, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));

            if (percent.compareTo(BigDecimal.valueOf(85)) > 0) {
                System.out.printf("High RAM usage (%.2f%%)%n", percent);
            }
        }

        // Disk warning
        if (diskTotal.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal freePercent = diskFree.divide(diskTotal, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));

            if (freePercent.compareTo(BigDecimal.valueOf(10)) < 0) {
                System.out.printf("Not enough disk space (%.2f%% free)%n", freePercent);
            }
        }
    }


    protected void recordSystemStats(Map<String, Object> data, User user) {
        try {
            BigDecimal cpu = toDecimal(data.get("cpuLoad"));
            BigDecimal ramUsed = toDecimal(data.get("ramUsed"));
            BigDecimal ramTotal = toDecimal(data.get("ramTotal"));

            BigDecimal diskTotal = toDecimal(data.get("diskTotal"));
            BigDecimal diskFree = toDecimal(data.get("diskFree"));
            BigDecimal diskUsed = toDecimal(data.get("diskUsed"));

            long uptimeSec = toLong(data.get("uptimeSeconds"));
            long keys = toLong(data.get("keys"));
            long clicks = toLong(data.get("clicks"));
            long moves = toLong(data.get("moves"));


            String window = safeStr(data.get("activeWindow"));

            SystemStats stats = new SystemStats();
            stats.setUser(user);
            stats.setCpuLoad(cpu);

            stats.setRamUsedMb(ramUsed);
            stats.setRamTotalMb(ramTotal);

            stats.setActiveWindow(window);
            stats.setKeyboardPresses((int) keys);
            stats.setMouseClicks((int) clicks);
            stats.setMouseMoves(moves);

            stats.setSystemUptimeSeconds(uptimeSec);

            stats.setDiskTotalGb(diskTotal);
            stats.setDiskFreeGb(diskFree);
            stats.setDiskUsedGb(diskUsed);

            statsRepository.save(stats);

        } catch (Exception e) {
            System.err.println("[MonitoringService] Error saving metrics: " + e.getMessage());
        }
    }

    protected BigDecimal toDecimal(Object obj) {
        if (obj instanceof BigDecimal bd) return bd;

        if (obj instanceof Number n) {
            return BigDecimal.valueOf(n.doubleValue());
        }

        try {
            return new BigDecimal(String.valueOf(obj));
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    protected long toLong(Object obj) {
        if (obj instanceof Number n) return n.longValue();
        try {
            return Long.parseLong(String.valueOf(obj));
        } catch (Exception e) {
            return 0L;
        }
    }

    protected String safeStr(Object obj) {
        return obj == null ? "" : obj.toString();
    }

    protected long parseUptimeToSeconds(String uptime) {
        if (uptime == null || uptime.equalsIgnoreCase("Unknown")) return 0;

        uptime = uptime.trim();
        long days = 0, hours = 0, minutes = 0;

        String[] parts = uptime.split(" ");
        for (int i = 0; i < parts.length - 1; i++) {
            if (parts[i].matches("\\d+")) {
                switch (parts[i + 1].toLowerCase()) {
                    case "d" -> days = Long.parseLong(parts[i]);
                    case "h" -> hours = Long.parseLong(parts[i]);
                    case "m" -> minutes = Long.parseLong(parts[i]);
                }
            }
        }

        return days * 86400 + hours * 3600 + minutes * 60;
    }

    private void tickFast() {
        if (!active) return;

        Map<String, Object> data = metricsProvider.collectAllMetrics();
        logWarnings(data);
    }

    private void tickSlow() {
        if (!active || activeUser == null) return;

        Map<String, Object> data = metricsProvider.collectAllMetrics();
        recordSystemStats(data, activeUser);
    }

    public void saveNow(User user) {
        if (user == null) {
            System.out.println("Guest mode — we don't save.");
            return;
        }

        try {
            Map<String, Object> data = collectFormattedStats();
            recordSystemStats(data, user);
        } catch (Exception e) {
            System.err.println("[MonitoringService] Error when saving manually: " + e.getMessage());
        }
    }

    public String formatStatusSaved() {
        return "Statistics manually updated (" + LocalTime.now().withNano(0) + ")";
    }
}
