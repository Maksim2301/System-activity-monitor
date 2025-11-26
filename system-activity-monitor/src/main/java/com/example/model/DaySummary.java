package com.example.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class DaySummary {

    private LocalDate date;
    private List<HourStat> hourlyStats;
    private BigDecimal uptimeHours;
    private BigDecimal cpuAvg;
    private BigDecimal ramAvg;
    private Map<String, BigDecimal> appUsagePercentByDay;

    public DaySummary() {}

    public DaySummary(LocalDate date, List<HourStat> hourlyStats, BigDecimal uptimeHours) {
        this.date = date;
        this.hourlyStats = hourlyStats;
        this.uptimeHours = uptimeHours;
    }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public List<HourStat> getHourlyStats() { return hourlyStats; }
    public void setHourlyStats(List<HourStat> hourlyStats) { this.hourlyStats = hourlyStats; }
    public BigDecimal getUptimeHours() { return uptimeHours; }
    public void setUptimeHours(BigDecimal uptimeHours) { this.uptimeHours = uptimeHours; }
    public BigDecimal getCpuAvg() { return cpuAvg; }
    public void setCpuAvg(BigDecimal cpuAvg) { this.cpuAvg = cpuAvg; }
    public BigDecimal getRamAvg() { return ramAvg; }
    public void setRamAvg(BigDecimal ramAvg) { this.ramAvg = ramAvg; }
    public Map<String, BigDecimal> getAppUsagePercentByDay() { return appUsagePercentByDay; }
    public void setAppUsagePercentByDay(Map<String, BigDecimal> appUsagePercentByDay) {
        this.appUsagePercentByDay = appUsagePercentByDay;
    }
}
