package org.example.ui.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.exception.InvalidLoginCredentialsException;
import org.example.exception.UserNotFoundException;
import org.example.model.User;
import org.example.service.AuthService;

import java.io.IOException;

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
                Scene scene = new Scene(loader.load(), 800, 600);
                Stage stage = (Stage) emailField.getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle("Workflow - Dashboard");
            } catch (IOException e) {
                throw new RuntimeException("Failed to load dashboard", e);
            }

        } catch (UserNotFoundException | InvalidLoginCredentialsException e) {
            errorLabel.setText(e.getMessage());
        }
    }
}
