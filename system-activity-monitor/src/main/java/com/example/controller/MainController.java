package com.example.controller;

import com.example.util.Session;
import com.example.modules.idle.service.IdleService;
import com.example.modules.monitoring.factory.EnvironmentFactoryProducer;
import com.example.modules.monitoring.factory.SystemEnvironmentFactory;
import com.example.modules.monitoring.service.MonitoringService;
import com.example.model.IdleTime;
import com.example.model.User;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.*;

public class MainController {


    @FXML private Label guestLabel;
    @FXML private Button userPanelBtn;
    @FXML private Button reportsBtn;
    @FXML private Button exitBtn;
    @FXML private Label idleStatusLabel;
    @FXML private Label idleMessage;
    @FXML private RadioButton idleOnline;
    @FXML private Label cpuLabel, ramLabel, osLabel, windowLabel;
    @FXML private Label keysLabel, clicksLabel, movesLabel;
    @FXML private Label uptimeLabel, diskLabel, statusLabel;
    @FXML private Button startMonitoringBtn;
    @FXML private Button stopMonitoringBtn;
    @FXML private Button updateNowBtn;

    private final IdleService idleService = new IdleService();
    private MonitoringService monitoringService;

    private ScheduledExecutorService uiUpdater;
    private boolean isMonitoring = false;
    private User activeUser;

    @FXML
    public void initialize() {
        activeUser = Session.getCurrentUser();

        if (Session.isGuest()) {
            guestLabel.setText("Guest Mode data is not saved.");
            idleMessage.setText("Guest Mode idle is not saved.");
        } else {
            guestLabel.setText("");
            idleMessage.setText("");
        }

        idleOnline.setSelected(true);
        updateIdleOnlineUI();
        enableButtons(true);

        stopMonitoringBtn.setDisable(true);
        statusLabel.setText("Monitoring stopped");
    }

    @FXML
    private void setIdleOnline() {
        if (Session.isGuest()) {
            idleMessage.setText("Guest: No data is recorded.");
            updateIdleOnlineUI();
            enableButtons(true);
            return;
        }
        try {
            IdleTime finished = idleService.endIdle(activeUser);

            if (finished != null) {
                Duration d = Duration.ofSeconds(finished.getDurationSeconds());
                idleMessage.setText(String.format(
                        "Idle completed: %d h %d sec.",
                        d.toMinutes(),
                        d.minusMinutes(d.toMinutes()).toSeconds()
                ));
            } else {
                idleMessage.setText("Idle didn't last.");
            }

        } catch (IllegalArgumentException e) {
            idleMessage.setText("User is not authorized..");
            showAlert("Log in.");
            return;
        }

        updateIdleOnlineUI();
        enableButtons(true);
    }

    private void updateIdleOnlineUI() {
        idleStatusLabel.setText("Status: Online");
        idleStatusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
    }

    @FXML
    private void setIdleOffline() {
        if (Session.isGuest()) {
            updateIdleOfflineUI();
            idleMessage.setText("Guest: not recorded idle.");
            enableButtons(false);
            return;
        }

        try {
            IdleTime started = idleService.startIdle(activeUser);
            idleMessage.setText("Idle start: " + started.getStartTime());

        } catch (IllegalStateException e) {
            idleMessage.setText("Idle is already underway.");
            return;

        } catch (IllegalArgumentException e) {
            idleMessage.setText("User is not authorized.");
            idleOnline.setSelected(true);
            showAlert("Log in first!");
            return;
        }

        updateIdleOfflineUI();
        enableButtons(false);
    }

    private void updateIdleOfflineUI() {
        idleStatusLabel.setText("Status: Offline");
        idleStatusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
    }

    private void enableButtons(boolean enabled) {
        userPanelBtn.setDisable(!enabled);
        reportsBtn.setDisable(!enabled);

        startMonitoringBtn.setDisable(!enabled);
        stopMonitoringBtn.setDisable(!enabled);
        updateNowBtn.setDisable(!enabled);

        exitBtn.setDisable(false);
    }

    // MONITORING START
    @FXML
    private void startMonitoring() {
        if (isMonitoring) {
            showAlert("Monitoring is already active!");
            return;
        }

        if (!Session.isGuest() && (activeUser == null || activeUser.getId() == null)) {
            showAlert("Log in first!");
            return;
        }

        try {
            SystemEnvironmentFactory factory = EnvironmentFactoryProducer.getFactory();
            monitoringService = factory.createMonitoringService();

            monitoringService.start(Session.isGuest() ? null : activeUser);

            startMonitoringBtn.setDisable(true);
            stopMonitoringBtn.setDisable(false);
            isMonitoring = true;

            startAutoUpdate();
            statusLabel.setText("Monitoring is active.");

        } catch (Exception e) {
            showAlert("Error: " + e.getMessage());
        }
    }

    @FXML
    private void stopMonitoring() {
        if (!isMonitoring)
            return;

        monitoringService.stop();
        isMonitoring = false;
        stopAutoUpdate();

        startMonitoringBtn.setDisable(false);
        stopMonitoringBtn.setDisable(true);

        statusLabel.setText("Monitoring has stopped.");
    }

    @FXML
    private void updateNow() {
        if (monitoringService == null) return;

        try {
            if (!Session.isGuest() && activeUser != null) {
                monitoringService.saveNow(activeUser);
                statusLabel.setText("Updated manually.");
            }
            refreshStats();

        } catch (Exception e) {
            showAlert("Error " + e.getMessage());
        }
    }

    private void startAutoUpdate() {
        uiUpdater = Executors.newSingleThreadScheduledExecutor();
        uiUpdater.scheduleAtFixedRate(() ->
                Platform.runLater(this::refreshStats), 0, 5, TimeUnit.SECONDS);
    }

    private void stopAutoUpdate() {
        if (uiUpdater != null) {
            uiUpdater.shutdownNow();
        }
    }

    private void refreshStats() {

        Map<String, Object> data = monitoringService.collectFormattedStats();
        if (data == null) return;

        cpuLabel.setText(data.get("cpuLoad") + " %");
        ramLabel.setText(data.get("ramUsed") + " / " + data.get("ramTotal") + " MB");
        osLabel.setText((String) data.get("osName"));
        windowLabel.setText((String) data.get("activeWindow"));
        uptimeLabel.setText((String) data.get("uptime"));

        diskLabel.setText(
                String.format("%.2f / %.2f GB (%s)",
                        data.get("diskUsed"),
                        data.get("diskTotal"),
                        data.get("diskDetails"))
        );

        keysLabel.setText("" + data.get("keys"));
        clicksLabel.setText("" + data.get("clicks"));
        movesLabel.setText("" + data.get("moves"));
    }

    @FXML
    private void openUserPanel() {
        switchScene("/fxml/user.fxml", "User Management");
    }

    @FXML
    private void openReportsPanel() {

        if (Session.isGuest()) {
            showAlert("The guest is not allowed to view reports.");
            return;
        }

        if (activeUser == null || activeUser.getId() == null) {
            showAlert("Log in first!");
            return;
        }

        switchScene("/fxml/reports.fxml", "Reports");
    }

    @FXML
    private void exitApp() {
        Session.logout();
        System.exit(0);
    }

    private void switchScene(String fxml, String title) {
        try {
            Scene scene = new Scene(FXMLLoader.load(getClass().getResource(fxml)));
            Stage stage = (Stage) Stage.getWindows().filtered(Window::isShowing).get(0);
            stage.setScene(scene);
            stage.setTitle(title);
        } catch (Exception e) {
            showAlert("Error: " + e.getMessage());
        }
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK);
        alert.showAndWait();
    }
}
