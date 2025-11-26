package com.example.modules.reports.export;

import com.example.modules.reports.builder.ReportExportPackage;
import com.example.model.DaySummary;
import com.example.model.HourStat;
import com.example.util.FontResolver;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import java.io.FileOutputStream;
import java.nio.file.*;

public class PdfReportExporter implements ReportExporter {

    private static final Path EXPORT_DIR = DownloadDirectoryResolver.getDownloadDirectory();

    @Override
    public Path export(ReportExportPackage pkg) throws Exception {

        Files.createDirectories(EXPORT_DIR);

        Path path = EXPORT_DIR.resolve(pkg.name + ".pdf");

        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(path.toFile()));
        document.open();

        BaseFont bf = FontResolver.resolveCyrillicFont();
        Font font = new Font(bf, 12);
        Font bold = new Font(bf, 16, Font.BOLD);

        document.add(new Paragraph("Звіт: " + pkg.name, bold));
        document.add(new Paragraph("Період: " + pkg.period, font));

        if (pkg.cpuAvg != null)
            document.add(new Paragraph("CPU Avg: " + pkg.cpuAvg + "%", font));

        if (pkg.ramAvg != null)
            document.add(new Paragraph("RAM Avg: " + pkg.ramAvg + " MB", font));

        if (pkg.idleSeconds != null)
            document.add(new Paragraph("Idle Time: " + pkg.idleSeconds + " сек", font));

        if (pkg.avgUptime != null)
            document.add(new Paragraph("Avg Uptime: " + pkg.avgUptime + " год/день", font));

        document.add(new Paragraph("\n"));

        document.add(new Paragraph("Середній CPU/RAM по днях:", bold));
        for (DaySummary d : pkg.days) {
            document.add(new Paragraph(
                    String.format("%s — CPU: %.2f%%, RAM: %.2f MB",
                            d.getDate(),
                            d.getCpuAvg(),
                            d.getRamAvg()
                    ),
                    font
            ));
        }
        document.add(new Paragraph("\n"));

        document.add(new Paragraph("Використання програм по днях:", bold));
        PdfPTable appDailyTable = new PdfPTable(3);
        appDailyTable.addCell("Дата");
        appDailyTable.addCell("Програма");
        appDailyTable.addCell("Відсоток");

        for (DaySummary d : pkg.days) {
            if (d.getAppUsagePercentByDay() != null) {
                for (var entry : d.getAppUsagePercentByDay().entrySet()) {
                    appDailyTable.addCell(d.getDate().toString());
                    appDailyTable.addCell(entry.getKey());
                    appDailyTable.addCell(entry.getValue().toString());
                }
            }
        }
        document.add(appDailyTable);
        document.add(new Paragraph("\n"));

        document.add(new Paragraph("Аптайм по днях:", bold));
        for (DaySummary d : pkg.days) {
            document.add(new Paragraph(
                    d.getDate() + " — " + d.getUptimeHours() + " год",
                    font
            ));
        }
        document.add(new Paragraph("\n"));

        boolean hasHourly = pkg.days.stream().anyMatch(d -> d.getHourlyStats() != null);
        if (hasHourly) {

            document.add(new Paragraph("Погодинна статистика:", bold));

            PdfPTable table = new PdfPTable(4);
            table.addCell("Дата");
            table.addCell("Година");
            table.addCell("CPU (%)");
            table.addCell("RAM (MB)");

            for (DaySummary d : pkg.days) {
                if (d.getHourlyStats() == null) continue;

                for (HourStat h : d.getHourlyStats()) {
                    table.addCell(d.getDate().toString());
                    table.addCell(String.format("%02d:00", h.getHour()));
                    table.addCell(h.getAvgCpu().toString());
                    table.addCell(h.getAvgRam().toString());
                }
            }

            document.add(table);
            document.add(new Paragraph("\n"));
        }

        if (pkg.appUsage != null && !pkg.appUsage.isEmpty()) {
            document.add(new Paragraph("Використання програм (Загалом):", bold));

            PdfPTable appTable = new PdfPTable(2);
            appTable.addCell("Програма");
            appTable.addCell("Відсоток");

            pkg.appUsage.forEach((app, value) -> {
                appTable.addCell(app);
                appTable.addCell(value + "%");
            });

            document.add(appTable);
        }

        document.close();
        return path;
    }

    @Override
    public String getExtension() {
        return "pdf";
    }
}
