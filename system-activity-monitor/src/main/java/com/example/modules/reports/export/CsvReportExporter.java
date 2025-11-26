package com.example.modules.reports.export;

import com.example.modules.reports.builder.ReportExportPackage;
import com.example.model.DaySummary;
import com.example.model.HourStat;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;

public class CsvReportExporter implements ReportExporter {

    private static final Path EXPORT_DIR = DownloadDirectoryResolver.getDownloadDirectory();

    @Override
    public Path export(ReportExportPackage pkg) throws IOException {

        Files.createDirectories(EXPORT_DIR);

        Path path = EXPORT_DIR.resolve(pkg.name + ".csv");

        try (FileWriter writer = new FileWriter(path.toFile())) {

            writer.write("Report," + pkg.name + "\n");
            writer.write("Period," + pkg.period + "\n");

            if (pkg.cpuAvg != null)
                writer.write("CPU Avg," + pkg.cpuAvg + "\n");

            if (pkg.ramAvg != null)
                writer.write("RAM Avg," + pkg.ramAvg + "\n");

            if (pkg.idleSeconds != null)
                writer.write("Idle Time," + pkg.idleSeconds + "\n");

            if (pkg.avgUptime != null)
                writer.write("Avg Uptime," + pkg.avgUptime + "\n");

            writer.write("\n");

            if (pkg.days != null && !pkg.days.isEmpty()) {
                writer.write("Date,Daily CPU Avg,Daily RAM Avg\n");
                for (DaySummary d : pkg.days) {
                    writer.write(String.format(
                            "%s,%.2f,%.2f\n",
                            d.getDate(),
                            d.getCpuAvg() != null ? d.getCpuAvg() : 0,
                            d.getRamAvg() != null ? d.getRamAvg() : 0
                    ));
                }
                writer.write("\n");
            }

            // DAILY APP USAGE
            if (pkg.days != null && !pkg.days.isEmpty()) {
                writer.write("Date,Application,Usage (%)\n");
                for (DaySummary d : pkg.days) {
                    if (d.getAppUsagePercentByDay() != null) {
                        for (var entry : d.getAppUsagePercentByDay().entrySet()) {
                            writer.write(String.format(
                                    "%s,%s,%.2f\n",
                                    d.getDate(),
                                    entry.getKey(),
                                    entry.getValue()
                            ));
                        }
                    }
                }
                writer.write("\n");
            }

            if (pkg.days != null && !pkg.days.isEmpty()) {
                writer.write("Date,Uptime Hours\n");
                for (DaySummary d : pkg.days) {
                    writer.write(d.getDate() + "," + d.getUptimeHours() + "\n");
                }
                writer.write("\n");
            }

            if (pkg.days != null && !pkg.days.isEmpty()) {
                writer.write("Date,Hour,CPU,RAM\n");
                for (DaySummary d : pkg.days) {
                    if (d.getHourlyStats() == null) continue;
                    for (HourStat h : d.getHourlyStats()) {
                        writer.write(String.format(
                                "%s,%02d:00,%.2f,%.2f\n",
                                d.getDate(),
                                h.getHour(),
                                h.getAvgCpu(),
                                h.getAvgRam()
                        ));
                    }
                }
                writer.write("\n");
            }

            if (pkg.appUsage != null && !pkg.appUsage.isEmpty()) {
                writer.write("Total Application Usage\n");
                writer.write("Application,Usage (%)\n");
                pkg.appUsage.forEach((k, v) -> {
                    try {
                        writer.write(k + "," + v + "\n");
                    } catch (IOException ignored) {}
                });
            }
        }

        return path;
    }

    @Override
    public String getExtension() {
        return "csv";
    }
}
