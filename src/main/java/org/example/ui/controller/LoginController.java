package org.example.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.example.exception.InvalidLoginCredentialsException;
import org.example.exception.UserNotFoundException;
import org.example.model.User;
import org.example.service.AuthService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

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

            // ouvrir dashboard dans une nouvelle fenêtre
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
            Scene scene = new Scene(loader.load());

            Stage newStage = new Stage();
            newStage.setTitle("ProjectRoom");
            newStage.setScene(scene);
            newStage.show();

            // fermer login
            Stage currentStage = (Stage) emailField.getScene().getWindow();
            currentStage.close();

        } catch (UserNotFoundException | InvalidLoginCredentialsException e){
            errorLabel.setText(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
