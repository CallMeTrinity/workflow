package org.example.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.model.UserStory;
import org.example.model.enums.Priority;
import org.example.service.UserStoryService;

public class CreateUserStoryController {

    @FXML private Label formTitle;
    @FXML private TextField titleField;
    @FXML private TextArea descriptionField;
    @FXML private ComboBox<Priority> priorityBox;
    @FXML private Label errorLabel;
    @FXML private Button submitButton;

    private final UserStoryService userStoryService = new UserStoryService();
    private Long projectId;
    private UserStory userStoryToEdit = null;

    @FXML
    public void initialize() {
        priorityBox.getItems().addAll(Priority.values());
        priorityBox.setValue(Priority.MEDIUM);
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public void setUserStory(UserStory userStory) {
        this.userStoryToEdit = userStory;

        titleField.setText(userStory.getTitle());
        descriptionField.setText(userStory.getDescription());
        priorityBox.setValue(userStory.getPriority());

        formTitle.setText("Modifier la User Story");
        submitButton.setText("Modifier");
    }

    @FXML
    private void handleCreate() {
        String title = titleField.getText().trim();

        if (title.isEmpty()) {
            errorLabel.setText("Le titre est obligatoire");
            return;
        }

        try {
            if (userStoryToEdit == null) {
                userStoryService.createUserStory(
                        title,
                        descriptionField.getText(),
                        priorityBox.getValue(),
                        projectId
                );
            } else {
                userStoryToEdit.setTitle(title);
                userStoryToEdit.setDescription(descriptionField.getText());
                userStoryToEdit.setPriority(priorityBox.getValue());

                userStoryService.updateUserStory(userStoryToEdit);
            }

            ((Stage) titleField.getScene().getWindow()).close();

        } catch (Exception e) {
            errorLabel.setText(e.getMessage());
        }
    }
}
