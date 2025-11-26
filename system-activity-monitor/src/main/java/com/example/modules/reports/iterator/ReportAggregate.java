package com.example.modules.reports.iterator;

import com.example.model.HourStat;
import com.example.model.Report;

public class ReportAggregate implements Aggregate<HourStat> {

    private final Report report;

    public ReportAggregate(Report report) {
        this.report = report;
    }

    @Override
    public Iterator<HourStat> createIterator() {
        return new DeepReportIterator(report);
    }

    @Override
    public int size() {
        Iterator<HourStat> it = createIterator();
        int count = 0;

        for (it.first(); !it.isDone(); it.next()) {
            if (it.currentItem() != null) count++;
        }
        return count;
    }

    @Override
    public HourStat get(int index) {
        if (index < 0) return null;

        Iterator<HourStat> it = createIterator();
        int i = 0;

        for (it.first(); !it.isDone(); it.next()) {
            HourStat stat = it.currentItem();
            if (stat == null) continue;

            if (i == index) return stat;
            i++;
        }
        return null;
    }
}
