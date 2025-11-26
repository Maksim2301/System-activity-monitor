package com.example.modules.reports.builder;

import com.example.model.Report;

public interface ReportBuilder {
    void reset();
    void buildBasicInfo(Report report);
    void buildCpuRam(Report report);
    void buildIdle(Report report);
    void buildDailyUptime(Report report);
    void buildHourlyStats(Report report);
    void buildAppUsage(Report report);
    ReportExportPackage getExportPackage();
    Report getBuiltReport();
}
