package com.example.modules.monitoring.factory;

import com.example.modules.monitoring.metrics.MetricsProvider;
import com.example.modules.monitoring.metrics.impl.WindowsMetricsProvider;
import com.example.modules.monitoring.service.MonitoringService;

public class WindowsSystemFactory implements SystemEnvironmentFactory {

    private final MetricsProvider provider = new WindowsMetricsProvider();

    @Override
    public MetricsProvider createMetricsProvider() {
        return provider;
    }

    @Override
    public MonitoringService createMonitoringService() {
        return new MonitoringService(provider);
    }
}
