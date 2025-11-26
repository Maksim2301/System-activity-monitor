package com.example.modules.reports.export;

import com.example.modules.reports.builder.ReportExportPackage;

import java.nio.file.Path;

public interface ReportExporter {
    Path export(ReportExportPackage pkg) throws Exception;
    String getExtension();
}
