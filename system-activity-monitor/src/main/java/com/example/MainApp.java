package com.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/main.fxml")
            );

            Scene scene = new Scene(loader.load());
            stage.setTitle("System Activity Monitor");
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Помилка запуску JavaFX UI: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}