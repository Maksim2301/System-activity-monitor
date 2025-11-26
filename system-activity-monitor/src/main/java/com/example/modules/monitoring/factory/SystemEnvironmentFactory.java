package com.example.modules.monitoring.factory;

import com.example.modules.monitoring.metrics.MetricsProvider;
import com.example.modules.monitoring.service.MonitoringService;

public interface SystemEnvironmentFactory {

    MetricsProvider createMetricsProvider();

    MonitoringService createMonitoringService();

}
