package com.example.modules.reports.command;

import com.example.modules.reports.builder.*;
import com.example.model.Report;
import com.example.modules.reports.service.ReportService;
import com.example.modules.reports.export.*;

import java.nio.file.Path;

public class ExportReportCommand extends ReportCommand {

    private final ReportService receiver;
    private final Report report;
    private final String format;
    private final ReportOptions options;

    private Path exportedFile;

    public ExportReportCommand(
            ReportService receiver,
            Report report,
            String format,
            ReportOptions options
    ) {
        this.receiver = receiver;
        this.report = report;
        this.format = format;
        this.options = options;
    }

    @Override
    public boolean execute() {
        try {
            ReportBuilder builder = new DefaultReportBuilder();
            ReportDirector director = new ReportDirector(builder);

            ReportExportPackage pkg = director.buildExportPackage(report, options);

            receiver.setExporter(resolveExporter(format));

            exportedFile = receiver.export(pkg);

            return exportedFile != null;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private ReportExporter resolveExporter(String format) {
        return switch (format.toLowerCase()) {
            case "pdf" -> new PdfReportExporter();
            case "csv" -> new CsvReportExporter();
            case "excel", "xlsx" -> new ExcelReportExporter();
            default -> throw new IllegalArgumentException("Unknown export format: " + format);
        };
    }
}

