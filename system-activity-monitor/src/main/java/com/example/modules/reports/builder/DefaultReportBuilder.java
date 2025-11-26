package com.example.modules.reports.builder;

import com.example.model.Report;

import java.util.Collections;

public class DefaultReportBuilder implements ReportBuilder {

    private ReportExportPackage pkg;
    private Report built;

    @Override
    public void reset() {
        pkg = new ReportExportPackage();
        built = new Report();

        pkg.days = Collections.emptyList();
    }

    @Override
    public void buildBasicInfo(Report report) {
        pkg.name = report.getReportName();
        pkg.period = report.getPeriodStart() + " - " + report.getPeriodEnd();

        built.setUser(report.getUser());
        built.setReportName(report.getReportName());
        built.setPeriodStart(report.getPeriodStart());
        built.setPeriodEnd(report.getPeriodEnd());
    }

    @Override
    public void buildCpuRam(Report report) {
        pkg.cpuAvg = report.getCpuAvg();
        pkg.ramAvg = report.getRamAvg();

        built.setCpuAvg(report.getCpuAvg());
        built.setRamAvg(report.getRamAvg());
    }

    @Override
    public void buildIdle(Report report) {
        pkg.idleSeconds = report.getIdleTimeTotalSeconds();
        built.setIdleTimeTotalSeconds(report.getIdleTimeTotalSeconds());
    }

    @Override
    public void buildDailyUptime(Report report) {
        pkg.avgUptime = report.getAvgUptimeHours();
        built.setAvgUptimeHours(report.getAvgUptimeHours());
    }

    @Override
    public void buildHourlyStats(Report report) {
        pkg.days = report.getDays() != null ? report.getDays() : Collections.emptyList();
        built.setDays(pkg.days);
    }

    @Override
    public void buildAppUsage(Report report) {
        pkg.appUsage = report.getAppUsagePercent();
        built.setAppUsagePercent(report.getAppUsagePercent());
    }

    @Override
    public ReportExportPackage getExportPackage() {
        return pkg;
    }

    @Override
    public Report getBuiltReport() {
        return built;
    }
}
