package com.example.modules.reports.builder;

import com.example.model.Report;

public class ReportDirector {

    private final ReportBuilder builder;

    public ReportDirector(ReportBuilder builder) {
        this.builder = builder;
    }

    public ReportExportPackage buildExportPackage(Report report, ReportOptions opt) {

        builder.reset();

        builder.buildBasicInfo(report);

        if (opt.includeCpuRam) builder.buildCpuRam(report);
        if (opt.includeIdle) builder.buildIdle(report);
        if (opt.includeDailyUptime) builder.buildDailyUptime(report);
        if (opt.includeHourlyStats) builder.buildHourlyStats(report);
        if (opt.includeAppUsage) builder.buildAppUsage(report);

        return builder.getExportPackage();
    }

    public Report buildReport(Report base, ReportOptions opt) {

        builder.reset();

        builder.buildBasicInfo(base);

        if (opt.includeCpuRam) builder.buildCpuRam(base);
        if (opt.includeIdle) builder.buildIdle(base);
        if (opt.includeDailyUptime) builder.buildDailyUptime(base);
        if (opt.includeHourlyStats) builder.buildHourlyStats(base);
        if (opt.includeAppUsage) builder.buildAppUsage(base);

        return builder.getBuiltReport();
    }
}
