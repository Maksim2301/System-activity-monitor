package com.example.model;

import java.math.BigDecimal;

public class HourStat {

    private int hour;
    private BigDecimal avgCpu;
    private BigDecimal avgRam;
    private transient DaySummary parentDay;

    public HourStat() {}

    public HourStat(int hour, BigDecimal avgCpu, BigDecimal avgRam) {
        this.hour = hour;
        this.avgCpu = avgCpu;
        this.avgRam = avgRam;
    }

    public int getHour() { return hour; }
    public BigDecimal getAvgCpu() { return avgCpu; }
    public BigDecimal getAvgRam() { return avgRam; }

    public DaySummary getParentDay() {
        return parentDay;
    }

    public void setParentDay(DaySummary parentDay) {
        this.parentDay = parentDay;
    }
}
