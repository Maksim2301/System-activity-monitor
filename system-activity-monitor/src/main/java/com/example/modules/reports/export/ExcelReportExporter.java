package com.example.modules.reports.export;

import com.example.modules.reports.builder.ReportExportPackage;
import com.example.model.DaySummary;
import com.example.model.HourStat;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.nio.file.*;

public class ExcelReportExporter implements ReportExporter {

    private static final Path EXPORT_DIR = DownloadDirectoryResolver.getDownloadDirectory();

    @Override
    public Path export(ReportExportPackage pkg) throws Exception {
        Files.createDirectories(EXPORT_DIR);

        Path path = EXPORT_DIR.resolve(pkg.name + ".xlsx");

        try (Workbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet("Report");
            int row = 0;

            sheet.createRow(row++).createCell(0).setCellValue("Report Name: " + pkg.name);
            sheet.createRow(row++).createCell(0).setCellValue("Period: " + pkg.period);

            if (pkg.cpuAvg != null)
                sheet.createRow(row++).createCell(0).setCellValue("CPU Avg: " + pkg.cpuAvg);

            if (pkg.ramAvg != null)
                sheet.createRow(row++).createCell(0).setCellValue("RAM Avg: " + pkg.ramAvg);

            if (pkg.idleSeconds != null)
                sheet.createRow(row++).createCell(0).setCellValue("Idle: " + pkg.idleSeconds);

            if (pkg.avgUptime != null)
                sheet.createRow(row++).createCell(0).setCellValue("Avg Uptime: " + pkg.avgUptime);

            row += 2;

            if (pkg.days != null && !pkg.days.isEmpty()) {
                Row title = sheet.createRow(row++);
                title.createCell(0).setCellValue("Date");
                title.createCell(1).setCellValue("Daily CPU Avg (%)");
                title.createCell(2).setCellValue("Daily RAM Avg (MB)");

                for (DaySummary d : pkg.days) {
                    Row r = sheet.createRow(row++);
                    r.createCell(0).setCellValue(d.getDate().toString());
                    r.createCell(1).setCellValue(d.getCpuAvg().doubleValue());
                    r.createCell(2).setCellValue(d.getRamAvg().doubleValue());
                }

                row += 2;
            }

            if (pkg.days != null && !pkg.days.isEmpty()) {
                Row title = sheet.createRow(row++);
                title.createCell(0).setCellValue("Date");
                title.createCell(1).setCellValue("Application");
                title.createCell(2).setCellValue("Usage (%)");

                for (DaySummary d : pkg.days) {
                    if (d.getAppUsagePercentByDay() != null) {
                        for (var entry : d.getAppUsagePercentByDay().entrySet()) {
                            Row r = sheet.createRow(row++);
                            r.createCell(0).setCellValue(d.getDate().toString());
                            r.createCell(1).setCellValue(entry.getKey());
                            r.createCell(2).setCellValue(entry.getValue().doubleValue());
                        }
                    }
                }

                row += 2;
            }

            if (pkg.days != null && !pkg.days.isEmpty()) {
                Row title = sheet.createRow(row++);
                title.createCell(0).setCellValue("Date");
                title.createCell(1).setCellValue("Uptime (h)");

                for (DaySummary d : pkg.days) {
                    Row r = sheet.createRow(row++);
                    r.createCell(0).setCellValue(d.getDate().toString());
                    r.createCell(1).setCellValue(d.getUptimeHours().doubleValue());
                }

                row += 2;
            }

            boolean hasHourly = pkg.days.stream().anyMatch(d -> d.getHourlyStats() != null);
            if (hasHourly) {

                Row h = sheet.createRow(row++);
                h.createCell(0).setCellValue("Date");
                h.createCell(1).setCellValue("Hour");
                h.createCell(2).setCellValue("CPU (%)");
                h.createCell(3).setCellValue("RAM (MB)");

                for (DaySummary d : pkg.days) {
                    if (d.getHourlyStats() == null) continue;

                    for (HourStat hs : d.getHourlyStats()) {
                        Row r = sheet.createRow(row++);
                        r.createCell(0).setCellValue(d.getDate().toString());
                        r.createCell(1).setCellValue(hs.getHour());
                        r.createCell(2).setCellValue(hs.getAvgCpu().doubleValue());
                        r.createCell(3).setCellValue(hs.getAvgRam().doubleValue());
                    }
                }

                row += 2;
            }

            if (pkg.appUsage != null && !pkg.appUsage.isEmpty()) {
                Row t = sheet.createRow(row++);
                t.createCell(0).setCellValue("Application Usage (Total)");

                for (var entry : pkg.appUsage.entrySet()) {
                    Row r = sheet.createRow(row++);
                    r.createCell(0).setCellValue(entry.getKey());
                    r.createCell(1).setCellValue(entry.getValue().doubleValue());
                }
            }

            try (FileOutputStream out = new FileOutputStream(path.toFile())) {
                workbook.write(out);
            }
        }

        return path;
    }

    @Override
    public String getExtension() {
        return "xlsx";
    }
}
