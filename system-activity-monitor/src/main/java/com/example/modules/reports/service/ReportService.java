package com.example.modules.reports.service;

import com.example.model.*;
import com.example.modules.reports.builder.*;
import com.example.modules.reports.calculations.ReportCalculator;
import com.example.modules.reports.export.ReportExporter;
import com.example.repository.interfaces.IdleRepository;
import com.example.repository.interfaces.ReportRepository;
import com.example.repository.interfaces.StatsRepository;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ReportService {

    private final ReportRepository reportRepo;
    private final StatsRepository statsRepo;
    private final IdleRepository idleRepo;
    private final ReportCalculator calculator = new ReportCalculator();
    private ReportExporter exporter;

    public ReportService(ReportRepository rr, StatsRepository sr, IdleRepository ir) {
        this.reportRepo = rr;
        this.statsRepo = sr;
        this.idleRepo = ir;
    }

    public void setExporter(ReportExporter exporter) {
        this.exporter = exporter;
    }

    public Report generateReport(User user, String name, LocalDate start, LocalDate end, ReportOptions opt) {
        validateUser(user);
        validatePeriod(start, end);

        LocalDateTime from = start.atStartOfDay();
        LocalDateTime to = end.atTime(23, 59, 59);

        List<SystemStats> stats = statsRepo.findByUserIdAndRecordedAtBetween(user.getId(), from, to);
        List<IdleTime> idle = idleRepo.findByUserIdAndStartTimeBetween(user.getId(), from, to);

        Report base = new Report();
        base.setUser(user);
        base.setReportName(name);
        base.setPeriodStart(start);
        base.setPeriodEnd(end);

        base.setCpuAvg(calculator.average(stats, SystemStats::getCpuLoad));
        base.setRamAvg(calculator.average(stats, SystemStats::getRamUsedMb));
        base.setIdleTimeTotalSeconds(calculator.totalIdle(idle));
        base.setAppUsagePercent(calculator.appUsagePercent(stats));
        base.setDays(calculator.buildDaySummary(stats));
        base.setAvgUptimeHours(calculator.averageUptime(stats));

        ReportBuilder builder = new DefaultReportBuilder();
        ReportDirector director = new ReportDirector(builder);

        Report finalReport = director.buildReport(base, opt);

        reportRepo.save(finalReport);
        return finalReport;
    }

    public Report findById(Integer id) {
        return reportRepo.findById(id).orElse(null);
    }

    public List<Report> getReportsByUser(User user) {
        validateUser(user);
        return reportRepo.findByUserId(user.getId());
    }

    public List<Report> getReportsInPeriod(User user, LocalDate start, LocalDate end) {
        validateUser(user);
        validatePeriod(start, end);

        return reportRepo.findByUserIdAndPeriodBetween(
                user.getId(),
                start,
                end
        );
    }

    public void deleteReport(Integer id) {
        reportRepo.deleteById(id);
    }

    public Path export(ReportExportPackage pkg) throws Exception {
        if (exporter == null)
            throw new IllegalStateException("ReportExporter strategy is not set.");
        return exporter.export(pkg);
    }

    private void validateUser(User user) {
        if (user == null || user.getId() == null)
            throw new IllegalArgumentException("User is not defined.");
    }

    private void validatePeriod(LocalDate start, LocalDate end) {
        if (start == null || end == null)
            return;

        if (end.isBefore(start))
            throw new IllegalArgumentException("Invalid date range.");
    }
}
