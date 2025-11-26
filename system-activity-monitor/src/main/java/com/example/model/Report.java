package com.example.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class Report {

    private Integer id;
    private User user;
    private String reportName;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private BigDecimal cpuAvg;
    private BigDecimal ramAvg;
    private BigDecimal idleTimeTotalSeconds;
    private BigDecimal avgUptimeHours;
    private Map<String, BigDecimal> appUsagePercent;
    private String filePath;
    private LocalDateTime createdAt;
    private List<DaySummary> days;

    public Report() {
        this.createdAt = LocalDateTime.now();
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getReportName() { return reportName; }
    public void setReportName(String reportName) { this.reportName = reportName; }

    public LocalDate getPeriodStart() { return periodStart; }
    public void setPeriodStart(LocalDate periodStart) { this.periodStart = periodStart; }

    public LocalDate getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(LocalDate periodEnd) { this.periodEnd = periodEnd; }

    public BigDecimal getCpuAvg() { return cpuAvg; }
    public void setCpuAvg(BigDecimal cpuAvg) { this.cpuAvg = cpuAvg; }

    public BigDecimal getRamAvg() { return ramAvg; }
    public void setRamAvg(BigDecimal ramAvg) { this.ramAvg = ramAvg; }

    public BigDecimal getIdleTimeTotalSeconds() { return idleTimeTotalSeconds; }
    public void setIdleTimeTotalSeconds(BigDecimal idleTimeTotalSeconds) { this.idleTimeTotalSeconds = idleTimeTotalSeconds; }

    public BigDecimal getAvgUptimeHours() { return avgUptimeHours; }
    public void setAvgUptimeHours(BigDecimal avgUptimeHours) {            // ✅ ДОДАНО
        this.avgUptimeHours = avgUptimeHours;
    }

    public Map<String, BigDecimal> getAppUsagePercent() { return appUsagePercent; }
    public void setAppUsagePercent(Map<String, BigDecimal> appUsagePercent) { this.appUsagePercent = appUsagePercent; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<DaySummary> getDays() { return days; }
    public void setDays(List<DaySummary> days) { this.days = days; }
}
