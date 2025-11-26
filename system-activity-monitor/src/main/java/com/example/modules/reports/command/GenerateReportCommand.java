package com.example.modules.reports.command;

import com.example.modules.reports.builder.ReportOptions;
import com.example.model.Report;
import com.example.model.User;
import com.example.modules.reports.service.ReportService;

import java.time.LocalDate;

public class GenerateReportCommand extends ReportCommand {

    private final ReportService receiver;
    private final User user;
    private final String name;
    private final LocalDate start;
    private final LocalDate end;
    private final ReportOptions options;

    private Report created;

    public GenerateReportCommand(
            ReportService receiver,
            User user,
            String name,
            LocalDate start,
            LocalDate end,
            ReportOptions options
    ) {
        this.receiver = receiver;
        this.user = user;
        this.name = name;
        this.start = start;
        this.end = end;
        this.options = options;
    }

    @Override
    public boolean execute() {
        created = receiver.generateReport(user, name, start, end, options);
        return created != null && created.getId() != null;
    }
    public Report getCreated() {
        return created;
    }
}

