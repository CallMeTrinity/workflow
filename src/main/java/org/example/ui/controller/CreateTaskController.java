package org.example.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.example.model.Project;
import org.example.model.UserStory;
import org.example.model.enums.Priority;
import org.example.model.enums.Status;
import org.example.service.TaskService;
import org.example.service.UserStoryService;
import org.example.model.Task;

import java.time.LocalDate;
import java.util.List;

public class CreateTaskController {

    @FXML private TextField titleField;
    @FXML private TextField descriptionField;
    @FXML private DatePicker deadlinePicker;
    @FXML private ComboBox<Priority> priorityBox;
    @FXML private ComboBox<UserStory> userStoryBox;
    @FXML private Label errorLabel;

    private final TaskService taskService = new TaskService();
    private final UserStoryService userStoryService = new UserStoryService();
    private Project project;
    private Task taskToEdit = null;

    @FXML
    public void initialize() {
        priorityBox.getItems().addAll(Priority.values());
        priorityBox.setValue(Priority.MEDIUM);

        userStoryBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(UserStory us) {
                if (us == null) return "Aucune";
                return us.getTitle();
            }

            @Override
            public UserStory fromString(String string) {
                return null;
            }
        });
    }

    public void setProject(Project project) {
        this.project = project;
        loadUserStories();
    }

    private void loadUserStories() {
        List<UserStory> userStories = userStoryService.getUserStoriesByProject(project.getId());
        userStoryBox.getItems().clear();
        userStoryBox.getItems().add(null); // "Aucune" option
        userStoryBox.getItems().addAll(userStories);
        userStoryBox.setValue(null);
    }

    public void setTask(Task task) {
        this.taskToEdit = task;

        titleField.setText(task.getTitle());
        descriptionField.setText(task.getDescription());
        if (task.getDeadline() != null && !task.getDeadline().isEmpty()) {
            deadlinePicker.setValue(LocalDate.parse(task.getDeadline()));
        }
        priorityBox.setValue(task.getPriority());

        if (task.getUserStoryId() != null) {
            for (UserStory us : userStoryBox.getItems()) {
                if (us != null && us.getId().equals(task.getUserStoryId())) {
                    userStoryBox.setValue(us);
                    break;
                }
            }
        }
    }

    @FXML
    private void handleCreate() {
        String title = titleField.getText().trim();

        if (title.isEmpty()) {
            errorLabel.setText("Le titre est obligatoire");
            return;
        }

        String deadline = deadlinePicker.getValue() != null ? deadlinePicker.getValue().toString() : null;
        UserStory selectedUs = userStoryBox.getValue();
        Long userStoryId = selectedUs != null ? selectedUs.getId() : null;

        try {
            if (taskToEdit == null) {
                // CREATE
                taskService.createTask(
                        title,
                        descriptionField.getText(),
                        Status.TODO,
                        priorityBox.getValue(),
                        deadline,
                        null,
                        null,
                        project.getId(),
                        userStoryId
                );
            } else {
                // UPDATE
                taskToEdit.setTitle(title);
                taskToEdit.setDescription(descriptionField.getText());
                taskToEdit.setDeadline(deadline);
                taskToEdit.setPriority(priorityBox.getValue());
                taskToEdit.setUserStoryId(userStoryId);

                taskService.updateTask(taskToEdit);
            }

            ((Stage) titleField.getScene().getWindow()).close();

        } catch (Exception e) {
            errorLabel.setText(e.getMessage());
        }
    }
}
