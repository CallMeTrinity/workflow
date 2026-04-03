package org.example.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.model.Project;
import org.example.service.ProjectService;

import java.time.LocalDate;

public class EditProjectController {

    @FXML private TextField nameField;
    @FXML private TextField descriptionField;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Label errorLabel;

    private final ProjectService projectService = new ProjectService();
    private Project project;

    public void setProject(Project project) {
        this.project = project;
        nameField.setText(project.getName());
        descriptionField.setText(project.getDescription());
        if (project.getStartDate() != null && !project.getStartDate().isEmpty()) {
            startDatePicker.setValue(LocalDate.parse(project.getStartDate()));
        }
        if (project.getEndDate() != null && !project.getEndDate().isEmpty()) {
            endDatePicker.setValue(LocalDate.parse(project.getEndDate()));
        }
    }

    @FXML
    private void handleSave() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            errorLabel.setText("Le nom est obligatoire");
            return;
        }

        try {
            project.setName(name);
            project.setDescription(descriptionField.getText());
            project.setStartDate(startDatePicker.getValue() != null ? startDatePicker.getValue().toString() : null);
            project.setEndDate(endDatePicker.getValue() != null ? endDatePicker.getValue().toString() : null);
            projectService.updateProject(project);
            ((Stage) nameField.getScene().getWindow()).close();
        } catch (Exception e) {
            errorLabel.setText(e.getMessage());
        }
    }
}
