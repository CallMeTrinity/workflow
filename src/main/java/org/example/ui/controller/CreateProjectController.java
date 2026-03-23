package org.example.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.service.ProjectService;

public class CreateProjectController {

    @FXML
    private TextField nameField;

    @FXML
    private TextField descriptionField;

    @FXML
    private TextField startDateField;

    @FXML
    private TextField endDateField;

    private ProjectService projectService = new ProjectService();

    @FXML
    private void handleCreate() {

        try {
            projectService.createProject(
                    nameField.getText(),
                    descriptionField.getText(),
                    startDateField.getText(),
                    endDateField.getText()
            );

            // fermer la fenêtre
            Stage stage = (Stage) nameField.getScene().getWindow();
            stage.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}