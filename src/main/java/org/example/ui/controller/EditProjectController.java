package org.example.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.model.Project;
import org.example.service.ProjectService;

public class EditProjectController {

    @FXML private TextField nameField;
    @FXML private TextField descriptionField;
    @FXML private TextField startDateField;
    @FXML private TextField endDateField;
    @FXML private Label errorLabel;

    private final ProjectService projectService = new ProjectService();
    private Project project;

    public void setProject(Project project) {
        this.project = project;
        nameField.setText(project.getName());
        descriptionField.setText(project.getDescription());
        startDateField.setText(project.getStartDate());
        endDateField.setText(project.getEndDate());
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
            project.setStartDate(startDateField.getText());
            project.setEndDate(endDateField.getText());
            projectService.updateProject(project);
            ((Stage) nameField.getScene().getWindow()).close();
        } catch (Exception e) {
            errorLabel.setText(e.getMessage());
        }
    }
}
