package org.example.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.service.ProjectService;

public class CreateProjectController {

    @FXML private TextField nameField;
    @FXML private TextField descriptionField;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;

    private ProjectService projectService = new ProjectService();

    @FXML
    private void handleCreate() {

        try {
            String startDate = startDatePicker.getValue() != null ? startDatePicker.getValue().toString() : null;
            String endDate = endDatePicker.getValue() != null ? endDatePicker.getValue().toString() : null;

            projectService.createProject(
                    nameField.getText(),
                    descriptionField.getText(),
                    startDate,
                    endDate
            );

            // fermer la fenêtre
            Stage stage = (Stage) nameField.getScene().getWindow();
            stage.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
