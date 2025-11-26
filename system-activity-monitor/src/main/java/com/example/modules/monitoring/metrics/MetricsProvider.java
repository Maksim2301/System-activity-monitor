package com.example.modules.monitoring.metrics;

import java.math.BigDecimal;
import java.util.Map;

public interface MetricsProvider {

    BigDecimal getCpuLoad();
    BigDecimal getRamUsed();
    BigDecimal getRamTotal();
    void updateDiskStats();
    BigDecimal getDiskTotal();
    BigDecimal getDiskFree();
    BigDecimal getDiskUsed();
    String getActiveWindowTitle();
    String getUptime();
    void startInputMonitoring();
    void stopInputMonitoring();
    Map<String, Long> getInputStats();
    Map<String, Object> collectAllMetrics();
}
