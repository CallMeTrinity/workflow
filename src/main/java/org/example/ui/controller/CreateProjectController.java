package org.example.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import org.example.config.SessionManager;
import org.example.model.User;
import org.example.model.enums.Role;
import org.example.repository.UserRepository;
import org.example.service.ProjectService;
import org.example.ui.util.Modals;

import java.util.List;

public class CreateProjectController {

    @FXML private TextField nameField;
    @FXML private TextField descriptionField;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<User> leaderBox;
    @FXML private Label errorLabel;

    private final ProjectService projectService = new ProjectService();
    private final UserRepository userRepository = new UserRepository();

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

        // Pré-sélectionner l'utilisateur courant
        Long currentId = SessionManager.getCurrentUser().getId();
        users.stream().filter(u -> u.getId().equals(currentId)).findFirst()
                .ifPresent(leaderBox::setValue);
    }

    @FXML
    private void handleCreate() {
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
            String startDate = startDatePicker.getValue() != null ? startDatePicker.getValue().toString() : null;
            String endDate   = endDatePicker.getValue()   != null ? endDatePicker.getValue().toString()   : null;

            projectService.createProject(
                    name,
                    descriptionField.getText(),
                    startDate,
                    endDate,
                    leaderBox.getValue().getId()
            );

            Modals.close(nameField);

        } catch (Exception e) {
            errorLabel.setText(e.getMessage());
        }
    }
}
