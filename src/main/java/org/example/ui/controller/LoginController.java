package org.example.ui.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.example.exception.InvalidLoginCredentialsException;
import org.example.exception.UserNotFoundException;
import org.example.model.User;
import org.example.service.AuthService;

import java.io.IOException;
import java.util.Objects;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    private final AuthService authService = new AuthService();
    @FXML
    private void handleLogin() {

        String email = emailField.getText();
        String password = passwordField.getText();

        try {
            User user = authService.login(email, password);
            System.out.println("Logged in as : " + user.getFullName());

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
                Stage stage = (Stage) emailField.getScene().getWindow();
                Scene scene = stage.getScene();
                scene.setRoot(loader.load());
                stage.setTitle("Workflow - Dashboard");
            } catch (IOException e) {
                throw new RuntimeException("Failed to load dashboard", e);
            }

        } catch (UserNotFoundException | InvalidLoginCredentialsException e) {
            errorLabel.setText(e.getMessage());
        }
    }

    @FXML
    public void initialize() {
        passwordField.setOnKeyPressed(event -> {
            if (Objects.requireNonNull(event.getCode()) == KeyCode.ENTER) {
                handleLogin();
            }
        });
    }
}
