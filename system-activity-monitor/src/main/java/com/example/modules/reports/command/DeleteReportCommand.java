package com.example.modules.reports.command;

import com.example.model.Report;
import com.example.modules.reports.service.ReportService;

public class DeleteReportCommand extends ReportCommand {

    private final ReportService receiver;
    private final Integer reportId;
    private Report deletedReport;

    public DeleteReportCommand(ReportService receiver, Integer reportId) {
        this.receiver = receiver;
        this.reportId = reportId;
    }

    @Override
    public boolean execute() {
        deletedReport = receiver.findById(reportId);
        if (deletedReport == null) return false;

        receiver.deleteReport(reportId);
        return true;
    }
}
