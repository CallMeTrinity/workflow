package org.example.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.example.exception.InvalidLoginCredentialsException;
import org.example.exception.UserNotFoundException;
import org.example.model.User;
import org.example.service.AuthService;

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
            // @TODO naviguer vers le dashboard
        } catch (UserNotFoundException | InvalidLoginCredentialsException e){
            errorLabel.setText(e.getMessage());
        }
    }
}
