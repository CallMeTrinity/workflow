package org.example.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import org.example.model.Project;
import org.example.model.User;
import org.example.model.enums.Role;
import org.example.repository.UserRepository;
import org.example.service.ProjectService;
import org.example.ui.util.Modals;

import java.time.LocalDate;
import java.util.List;

public class EditProjectController {

    @FXML private TextField nameField;
    @FXML private TextField descriptionField;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<User> leaderBox;
    @FXML private Label errorLabel;

    private final ProjectService projectService = new ProjectService();
    private final UserRepository userRepository = new UserRepository();
    private Project project;

    @FXML
    public void initialize() {
        List<User> users = userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.ADMIN || u.getRole() == Role.PROJECT_LEADER)
                .toList();
        leaderBox.getItems().setAll(users);
        leaderBox.setConverter(new StringConverter<>() {
            @Override public String toString(User u) {
                return u == null ? "" : u.getFirstName() + " " + u.getLastName();
            }
            @Override public User fromString(String s) { return null; }
        });
    }

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

        // Pré-sélectionner le chef actuel
        leaderBox.getItems().stream()
                .filter(u -> u.getId().equals(project.getProjectLeaderId()))
                .findFirst()
                .ifPresent(leaderBox::setValue);
    }

    @FXML
    private void handleSave() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            errorLabel.setText("Le nom est obligatoire");
            return;
        }
        if (leaderBox.getValue() == null) {
            errorLabel.setText("Le chef de projet est obligatoire");
            return;
        }

        try {
            project.setName(name);
            project.setDescription(descriptionField.getText());
            project.setStartDate(startDatePicker.getValue() != null ? startDatePicker.getValue().toString() : null);
            project.setEndDate(endDatePicker.getValue() != null ? endDatePicker.getValue().toString() : null);
            project.setProjectLeaderId(leaderBox.getValue().getId());
            projectService.updateProject(project);
            Modals.close(nameField);
        } catch (Exception e) {
            errorLabel.setText(e.getMessage());
        }
    }
}
