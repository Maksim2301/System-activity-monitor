package com.example.controller;

import com.example.model.DaySummary;
import com.example.model.HourStat;
import com.example.modules.reports.builder.ReportOptions;
import com.example.modules.reports.command.*;
import com.example.repository.factory.RepositoryFactory;
import com.example.model.Report;
import com.example.model.User;
import com.example.modules.reports.service.ReportService;
import com.example.util.Session;
import javafx.scene.layout.VBox;
import com.example.modules.reports.iterator.*;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class ReportsController {

    @FXML private TextField reportNameField;
    @FXML private DatePicker startDatePicker, endDatePicker;
    @FXML private Label messageLabel;

    @FXML private ListView<Report> reportsList;

    @FXML private Label avgCpuLabel;
    @FXML private Label avgRamLabel;
    @FXML private Label uptimeAvgLabel;
    @FXML private Label idleLabel;
    @FXML private VBox detailedStatsContainer;
    @FXML private CheckBox includeIdle;
    @FXML private CheckBox includeCpuRam;
    @FXML private CheckBox includeDailyUptime;
    @FXML private CheckBox includeHourlyStats;
    @FXML private CheckBox includeAppUsage;

    @FXML private ListView<String> appUsageList;

    private final ReportService reportService;
    private final Invoker commandManager = new Invoker();

    public ReportsController() {
        this.reportService = new ReportService(
                RepositoryFactory.getReportRepository(),
                RepositoryFactory.getStatsRepository(),
                RepositoryFactory.getIdleRepository()
        );
    }

    @FXML
    public void initialize() {
        reportsList.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(Report report, boolean empty) {
                super.updateItem(report, empty);

                if (empty || report == null) {
                    setText(null);
                    return;
                }
                setText(String.format(
                        "%s\n %s → %s",
                        report.getReportName(),
                        report.getPeriodStart(),
                        report.getPeriodEnd()
                ));
            }
        });
        reportsList.setPlaceholder(new Label("There are no reports yet."));
        reportsList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                displayReportDetails(newVal);
            }
        });
    }

    @FXML
    private void generateReport() {

        User user = Session.getCurrentUser();
        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();
        String name = reportNameField.getText();

        if (user == null || start == null || end == null || name.isBlank()) {
            messageLabel.setText("Fill in all fields.");
            return;
        }

        if (end.isBefore(start)) {
            messageLabel.setText("The end date cannot be earlier than the start date.");
            return;
        }

        ReportOptions options = new ReportOptions();
        options.includeCpuRam = includeCpuRam.isSelected();
        options.includeIdle = includeIdle.isSelected();
        options.includeDailyUptime = includeDailyUptime.isSelected();
        options.includeHourlyStats = includeHourlyStats.isSelected();
        options.includeAppUsage = includeAppUsage.isSelected();

        ReportCommand cmd = new GenerateReportCommand(
                reportService,
                user,
                name,
                start,
                end,
                options
        );

        if (commandManager.execute(cmd)) {
            refreshReports(user);
            messageLabel.setText("The report has been created.");
        } else {
            messageLabel.setText("Report creation failed.");
        }
    }

    @FXML
    private void deleteReport() {

        Report selected = reportsList.getSelectionModel().getSelectedItem();

        if (selected == null) {
            messageLabel.setText("Select a report.");
            return;
        }

        ReportCommand cmd = new DeleteReportCommand(reportService, selected.getId());

        if (commandManager.execute(cmd)) {
            refreshReports(Session.getCurrentUser());
            messageLabel.setText("Report deleted.");
        } else {
            messageLabel.setText("The report could not be deleted.");
        }
    }

    // -----------------------------------------------------------------------
    // Export report
    // -----------------------------------------------------------------------
    @FXML private void exportCSV() { exportSelected("csv"); }
    @FXML private void exportExcel() { exportSelected("excel"); }
    @FXML private void exportPDF() { exportSelected("pdf"); }

    private void exportSelected(String format) {

        Report selected = reportsList.getSelectionModel().getSelectedItem();

        if (selected == null) {
            messageLabel.setText("Select a report.");
            return;
        }

        ReportOptions options = new ReportOptions();
        options.includeCpuRam = includeCpuRam.isSelected();
        options.includeIdle = includeIdle.isSelected();
        options.includeDailyUptime = includeDailyUptime.isSelected();
        options.includeHourlyStats = includeHourlyStats.isSelected();
        options.includeAppUsage = includeAppUsage.isSelected();

        ReportCommand cmd = new ExportReportCommand(
                reportService,
                selected,
                format,
                options
        );

        if (commandManager.execute(cmd)) {
            messageLabel.setText("Exported (" + format.toUpperCase() + ")");
        } else {
            messageLabel.setText("Export error.");
        }
    }

    @FXML
    private void showReports() {

        User user = Session.getCurrentUser();

        if (user == null) {
            messageLabel.setText("Log in.");
            return;
        }

        refreshReports(user);
        messageLabel.setText("Reports updated.");
    }

    private void fillDetailedStats(Report report) {

        detailedStatsContainer.getChildren().clear();

        if (report.getDays() == null || report.getDays().isEmpty()) {
            detailedStatsContainer.getChildren().add(new Label("No detailed data."));
            return;
        }

        ReportAggregate aggregate = new ReportAggregate(report);
        Iterator<HourStat> it = aggregate.createIterator();

        it.first();
        if (it.isDone()) {
            detailedStatsContainer.getChildren().add(new Label("No hourly data."));
            return;
        }

        LocalDate currentDay = null;

        while (!it.isDone()) {

            HourStat hour = it.currentItem();

            DaySummary parentDay = hour.getParentDay();
            if (parentDay == null) {
                parentDay = report.getDays().stream()
                        .filter(d -> d.getHourlyStats() != null && d.getHourlyStats().contains(hour))
                        .findFirst()
                        .orElse(null);
            }

            if (parentDay != null) {
                if (currentDay == null || !parentDay.getDate().equals(currentDay)) {
                    currentDay = parentDay.getDate();

                    Label dateLabel = new Label("📅 " + currentDay);
                    dateLabel.setStyle("-fx-font-weight: bold; -fx-padding: 6 0 2 0;");
                    detailedStatsContainer.getChildren().add(dateLabel);

                    Label uptimeLabel = new Label(String.format(
                            "Uptime: %.2f h",
                            parentDay.getUptimeHours() != null
                                    ? parentDay.getUptimeHours().doubleValue()
                                    : 0.0
                    ));
                    uptimeLabel.setStyle("-fx-padding: 0 0 4 10; -fx-text-fill: #444;");
                    detailedStatsContainer.getChildren().add(uptimeLabel);

                    Label cpuDaily = new Label(String.format(
                            "Avg CPU per day: %.2f%%",
                            parentDay.getCpuAvg() != null ? parentDay.getCpuAvg().doubleValue() : 0.0
                    ));
                    cpuDaily.setStyle("-fx-padding: 0 0 2 10;");
                    detailedStatsContainer.getChildren().add(cpuDaily);

                    Label ramDaily = new Label(String.format(
                            "Avg RAM per day: %.2f MB",
                            parentDay.getRamAvg() != null ? parentDay.getRamAvg().doubleValue() : 0.0
                    ));
                    ramDaily.setStyle("-fx-padding: 0 0 4 10;");
                    detailedStatsContainer.getChildren().add(ramDaily);

                    if (includeAppUsage.isSelected()) {

                        Label appLabel = new Label("App usage per day:");
                        appLabel.setStyle("-fx-padding: 4 0 0 10; -fx-font-style: italic;");
                        detailedStatsContainer.getChildren().add(appLabel);

                        if (parentDay.getAppUsagePercentByDay() != null) {
                            parentDay.getAppUsagePercentByDay().forEach((app, percent) -> {
                                detailedStatsContainer.getChildren().add(
                                        new Label(String.format("    %s → %.2f%%", app, percent))
                                );
                            });
                        }
                    }
                }
                Label hourLabel = new Label(String.format(
                        "  %02d:00 — CPU: %.2f%% | RAM: %.2f MB",
                        hour.getHour(),
                        hour.getAvgCpu(),
                        hour.getAvgRam()
                ));
                detailedStatsContainer.getChildren().add(hourLabel);
            }

            it.next();
        }

        detailedStatsContainer.getChildren().add(new Label(""));
    }

    @FXML
    private void filterReportsByDate() {

        User user = Session.getCurrentUser();
        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();

        if (user == null || start == null || end == null) {
            messageLabel.setText("Select dates.");
            return;
        }

        if (end.isBefore(start)) {
            messageLabel.setText("The end date is before the start date.");
            return;
        }

        List<Report> filtered = reportService.getReportsInPeriod(user, start, end);

        reportsList.getItems().setAll(filtered);

        if (!filtered.isEmpty()) {
            displayReportDetails(filtered.get(0));
        }

        messageLabel.setText("Found: " + filtered.size());
    }

    private void displayReportDetails(Report report) {

        if (includeCpuRam.isSelected()) {
            double cpu = report.getCpuAvg() != null ? report.getCpuAvg().doubleValue() : 0.0;
            double ram = report.getRamAvg() != null ? report.getRamAvg().doubleValue() : 0.0;

            avgCpuLabel.setText(String.format("CPU: %.2f%%", cpu));
            avgRamLabel.setText(String.format("RAM: %.2f MB", ram));
        } else {
            avgCpuLabel.setText("CPU: —");
            avgRamLabel.setText("RAM: —");
        }

        if (includeIdle.isSelected()) {

            int totalSec = report.getIdleTimeTotalSeconds() != null
                    ? report.getIdleTimeTotalSeconds().intValue()
                    : 0;

            int hours = totalSec / 3600;
            int minutes = (totalSec % 3600) / 60;
            int seconds = totalSec % 60;

            idleLabel.setText(String.format("Idle: %d h %d min %d sec", hours, minutes, seconds));

        } else {
            idleLabel.setText("Idle: —");
        }

        if (includeDailyUptime.isSelected()) {

            BigDecimal avg = report.getAvgUptimeHours();
            if (avg != null) {
                uptimeAvgLabel.setText(
                        String.format("Avg uptime: %.2f h/day", avg.doubleValue())
                );
            } else {
                uptimeAvgLabel.setText("Avg uptime: —");
            }

        } else {
            uptimeAvgLabel.setText("Avg uptime: —");
        }

        appUsageList.getItems().clear();

        if (includeAppUsage.isSelected()) {
            Map<String, BigDecimal> apps = report.getAppUsagePercent();

            if (apps == null || apps.isEmpty()) {
                appUsageList.getItems().add("Немає даних");
            } else {
                apps.forEach((name, percent) -> {
                    double p = percent != null ? percent.doubleValue() : 0.0;
                    appUsageList.getItems().add(String.format("%s → %.2f%%", name, p));
                });
            }
        } else {
            appUsageList.getItems().add("— Disabled —");
        }

        detailedStatsContainer.getChildren().clear();

        if (includeHourlyStats.isSelected()) {
            fillDetailedStats(report);
        } else {
            detailedStatsContainer.getChildren().add(new Label("Detailed stats disabled."));
        }
    }

    @FXML
    private void goBack() {
        switchScene("/fxml/main.fxml", "Main Menu");
    }

    private void switchScene(String fxml, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) messageLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle(title);
        } catch (Exception e) {
            messageLabel.setText(" " + e.getMessage());
        }
    }

    private void refreshReports(User user) {
        try {
            List<Report> list = reportService.getReportsByUser(user);
            reportsList.getItems().setAll(list);

            Report selected = reportsList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                displayReportDetails(selected);
            } else if (!list.isEmpty()) {
                reportsList.getSelectionModel().select(0);
                displayReportDetails(list.get(0));
            }
        } catch (Exception e) {
            messageLabel.setText(" " + e.getMessage());
        }
    }
}
