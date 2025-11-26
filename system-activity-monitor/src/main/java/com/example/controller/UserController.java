package com.example.controller;

import com.example.dto.UserLoginDto;
import com.example.dto.UserPasswordChangeDto;
import com.example.dto.UserRegisterDto;
import com.example.repository.factory.RepositoryFactory;
import com.example.model.User;
import com.example.modules.user.service.UserService;
import com.example.util.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.Optional;

public class UserController {

    @FXML private TextField registerUsername;
    @FXML private PasswordField registerPassword;
    @FXML private TextField registerEmail;

    @FXML private TextField loginUsername;
    @FXML private PasswordField loginPassword;

    @FXML private PasswordField oldPassword;
    @FXML private PasswordField newPassword;

    @FXML private Label messageLabel;

    private final UserService userService;

    public UserController() {
        this.userService = new UserService(
                RepositoryFactory.getUserRepository()
        );
    }

    @FXML
    private void registerUser() {
        try {
            UserRegisterDto dto = new UserRegisterDto(
                    registerUsername.getText(),
                    registerPassword.getText(),
                    registerEmail.getText()
            );

            userService.register(dto);

            messageLabel.setText("User successfully created!");

        } catch (Exception e) {
            messageLabel.setText(" " + e.getMessage());
        }
    }

    @FXML
    private void loginUser() {

        UserLoginDto dto = new UserLoginDto(
                loginUsername.getText(),
                loginPassword.getText()
        );

        Optional<User> userOpt = userService.login(dto);

        if (userOpt.isPresent()) {
            User loggedUser = userOpt.get();

            Session.setCurrentUser(loggedUser);

            messageLabel.setText("Welcome, " + loggedUser.getUsername() + "!");
            switchScene("/fxml/main.fxml");

        } else {
            messageLabel.setText("Incorrect name or password.");
        }
    }

    @FXML
    private void changePassword() {

        User user = Session.getCurrentUser();

        if (user == null) {
            messageLabel.setText("First, log in.");
            return;
        }

        UserPasswordChangeDto dto = new UserPasswordChangeDto(
                oldPassword.getText(),
                newPassword.getText()
        );

        try {
            userService.changePassword(user.getId(), dto);

            messageLabel.setText("Password successfully changed!");

        } catch (Exception e) {
            messageLabel.setText(" " + e.getMessage());
        }
    }

    @FXML
    private void deleteMyAccount() {

        User user = Session.getCurrentUser();
        if (user == null) {
            messageLabel.setText(" You are not logged in.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Delete account?");
        confirm.setContentText("This action cannot be undone.");

        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                userService.deleteUser(user.getId());
                Session.logout();

                messageLabel.setText("Account deleted.");
                switchScene("/fxml/user.fxml");

            } catch (Exception e) {
                messageLabel.setText(" " + e.getMessage());
            }
        } else {
            messageLabel.setText("Cancelled.");
        }
    }

    @FXML
    private void loginAsGuest() {
        Session.setGuestMode();
        messageLabel.setText("Logged in as a guest.");
        switchScene("/fxml/main.fxml");
    }
    @FXML
    private void goBack() {
        switchScene("/fxml/main.fxml");
    }

    private void switchScene(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Scene scene = new Scene(loader.load());

            Stage stage = (Stage) messageLabel.getScene().getWindow();
            stage.setScene(scene);

        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Error transitioning between scenes.");
        }
    }
}
