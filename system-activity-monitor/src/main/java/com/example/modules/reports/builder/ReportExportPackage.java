package com.example.modules.reports.builder;

import com.example.model.DaySummary;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class ReportExportPackage {

    public String name;
    public String period;

    public BigDecimal cpuAvg;
    public BigDecimal ramAvg;
    public BigDecimal idleSeconds;
    public BigDecimal avgUptime;

    public List<DaySummary> days;
    public Map<String, BigDecimal> appUsage;
}
