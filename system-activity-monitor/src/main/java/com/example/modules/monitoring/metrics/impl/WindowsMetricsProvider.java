package com.example.modules.monitoring.metrics.impl;

import com.example.modules.monitoring.metrics.MetricsProvider;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class WindowsMetricsProvider implements MetricsProvider {

    private long lastIdle = 0, lastKernel = 0, lastUser = 0;
    private volatile BigDecimal cpuLoad = BigDecimal.ZERO;

    private final ScheduledExecutorService cpuScheduler =
            Executors.newSingleThreadScheduledExecutor();

    public WindowsMetricsProvider() {
        cpuScheduler.scheduleAtFixedRate(this::updateCpu, 0, 1, TimeUnit.SECONDS);
    }

    private long filetime(WinBase.FILETIME ft) {
        return (((long) ft.dwHighDateTime) << 32) | (ft.dwLowDateTime & 0xffffffffL);
    }

    private void updateCpu() {
        Kernel32 k = Kernel32.INSTANCE;

        WinBase.FILETIME idle = new WinBase.FILETIME();
        WinBase.FILETIME kernel = new WinBase.FILETIME();
        WinBase.FILETIME user = new WinBase.FILETIME();

        if (!k.GetSystemTimes(idle, kernel, user)) return;

        long idleNow = filetime(idle);
        long kernNow = filetime(kernel);
        long userNow = filetime(user);

        if (lastIdle == 0) {
            lastIdle = idleNow;
            lastKernel = kernNow;
            lastUser = userNow;
            return;
        }

        long idleDiff = idleNow - lastIdle;
        long kernDiff = kernNow - lastKernel;
        long userDiff = userNow - lastUser;

        lastIdle = idleNow;
        lastKernel = kernNow;
        lastUser = userNow;

        long total = kernDiff + userDiff;
        if (total <= 0) return;

        double usage = (double) (total - idleDiff) / total * 100.0;
        usage = Math.max(0, Math.min(100, usage));

        cpuLoad = BigDecimal.valueOf(usage).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal getCpuLoad() {
        return cpuLoad;
    }

    private BigDecimal ramTotal = BigDecimal.ZERO;

    @Override
    public BigDecimal getRamUsed() {
        Kernel32 k = Kernel32.INSTANCE;

        WinBase.MEMORYSTATUSEX mem = new WinBase.MEMORYSTATUSEX();
        if (!k.GlobalMemoryStatusEx(mem)) return BigDecimal.ZERO;

        long total = mem.ullTotalPhys.longValue();
        long free = mem.ullAvailPhys.longValue();

        ramTotal = BigDecimal.valueOf(total / 1024.0 / 1024.0).setScale(2, RoundingMode.HALF_UP);

        double usedMb = (total - free) / 1024.0 / 1024.0;
        return BigDecimal.valueOf(usedMb).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal getRamTotal() {
        return ramTotal;
    }

    private BigDecimal diskTotal = BigDecimal.ZERO;
    private BigDecimal diskFree = BigDecimal.ZERO;
    private String diskDetails = "Unknown";

    @Override
    public void updateDiskStats() {
        Kernel32 k = Kernel32.INSTANCE;

        double tot = 0, free = 0;
        StringBuilder sb = new StringBuilder();

        for (char d = 'C'; d <= 'Z'; d++) {
            String root = d + ":\\";

            if (k.GetDriveType(root) != WinBase.DRIVE_FIXED) continue;

            WinNT.LARGE_INTEGER freeAvail = new WinNT.LARGE_INTEGER();
            WinNT.LARGE_INTEGER totalBytes = new WinNT.LARGE_INTEGER();
            WinNT.LARGE_INTEGER freeBytes = new WinNT.LARGE_INTEGER();

            if (k.GetDiskFreeSpaceEx(root, freeAvail, totalBytes, freeBytes)) {

                double tGb = totalBytes.getValue() / 1e9;
                double fGb = freeBytes.getValue() / 1e9;

                tot += tGb;
                free += fGb;

                sb.append(String.format("%s: %.2f / %.2f GB | ", d, (tGb - fGb), tGb));
            }
        }

        diskTotal = BigDecimal.valueOf(tot).setScale(2, RoundingMode.HALF_UP);
        diskFree = BigDecimal.valueOf(free).setScale(2, RoundingMode.HALF_UP);
        diskDetails = sb.isEmpty() ? "Unknown" : sb.toString();
    }

    @Override public BigDecimal getDiskTotal() { return diskTotal; }
    @Override public BigDecimal getDiskFree()  { return diskFree;  }
    @Override public BigDecimal getDiskUsed()  { return diskTotal.subtract(diskFree); }

    @Override
    public String getUptime() {
        long sec = Kernel32.INSTANCE.GetTickCount64() / 1000;

        long d = sec / 86400;
        long h = (sec % 86400) / 3600;
        long m = (sec % 3600) / 60;

        return d + " d " + h + " h " + m + " m";
    }

    public long getSystemUptimeSecondsRaw() {
        return Kernel32.INSTANCE.GetTickCount64() / 1000;
    }

    private final AtomicInteger keyPressCount = new AtomicInteger();
    private final AtomicInteger mouseClickCount = new AtomicInteger();
    private final AtomicLong mouseMoveCount = new AtomicLong();
    private long lastActivity = System.currentTimeMillis();

    private ScheduledExecutorService inputScheduler;
    private boolean inputMonitoringActive = false;
    private int lastX = -1, lastY = -1;

    @Override
    public void startInputMonitoring() {
        if (inputMonitoringActive) return;

        inputMonitoringActive = true;

        inputScheduler = Executors.newSingleThreadScheduledExecutor();
        inputScheduler.scheduleAtFixedRate(this::checkInput, 0, 80, TimeUnit.MILLISECONDS);
    }

    private void checkInput() {
        User32 u = User32.INSTANCE;

        for (int i = 0x08; i <= 0xFE; i++) {
            if ((u.GetAsyncKeyState(i) & 0x0001) != 0) {
                keyPressCount.incrementAndGet();
                lastActivity = System.currentTimeMillis();
            }
        }

        // мишка
        if ((u.GetAsyncKeyState(0x01) & 1) != 0) mouseClickCount.incrementAndGet();
        if ((u.GetAsyncKeyState(0x02) & 1) != 0) mouseClickCount.incrementAndGet();

        WinDef.POINT p = new WinDef.POINT();
        u.GetCursorPos(p);

        if (lastX != -1 && lastY != -1) {
            if (Math.abs(p.x - lastX) > 3 || Math.abs(p.y - lastY) > 3) {
                mouseMoveCount.incrementAndGet();
                lastActivity = System.currentTimeMillis();
            }
        }

        lastX = p.x;
        lastY = p.y;
    }

    @Override
    public void stopInputMonitoring() {
        inputMonitoringActive = false;
        if (inputScheduler != null) inputScheduler.shutdownNow();
    }

    @Override
    public Map<String, Long> getInputStats() {
        if (!inputMonitoringActive) return Map.of(
                "keys", 0L,
                "clicks", 0L,
                "moves", 0L
        );

        Map<String, Long> map = new HashMap<>();
        map.put("keys", (long) keyPressCount.get());
        map.put("clicks", (long) mouseClickCount.get());
        map.put("moves", mouseMoveCount.get());
        return map;
    }

    @Override
    public String getActiveWindowTitle() {
        try {
            char[] buffer = new char[512];

            WinDef.HWND hwnd = User32.INSTANCE.GetForegroundWindow();
            if (hwnd == null) return "Unknown";

            User32.INSTANCE.GetWindowText(hwnd, buffer, 512);
            return Native.toString(buffer).trim();
        } catch (Exception e) {
            return "Unknown";
        }
    }

    @Override
    public Map<String, Object> collectAllMetrics() {
        updateDiskStats();

        Map<String, Object> map = new HashMap<>();
        map.put("cpuLoad", getCpuLoad());
        map.put("ramUsed", getRamUsed());
        map.put("ramTotal", getRamTotal());

        map.put("diskTotal", getDiskTotal());
        map.put("diskFree", getDiskFree());
        map.put("diskUsed", getDiskUsed());
        map.put("diskDetails", diskDetails);

        map.put("uptime", getUptime());
        map.put("uptimeSeconds", getSystemUptimeSecondsRaw());

        map.put("activeWindow", getActiveWindowTitle());
        map.put("osName", "Windows");

        if (inputMonitoringActive) map.putAll(getInputStats());

        return map;
    }
}
