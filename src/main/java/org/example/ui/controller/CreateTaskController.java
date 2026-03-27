package org.example.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.model.Project;
import org.example.model.enums.Priority;
import org.example.model.enums.Status;
import org.example.service.TaskService;
import org.example.model.Task;
import org.example.repository.TaskRepository;

public class CreateTaskController {

    @FXML private TextField titleField;
    @FXML private TextField descriptionField;
    @FXML private TextField deadlineField;
    @FXML private ComboBox<Priority> priorityBox;
    @FXML private Label errorLabel;

    private final TaskService taskService = new TaskService();
    private Project project;
    private Task taskToEdit = null;

    @FXML
    public void initialize() {
        priorityBox.getItems().addAll(Priority.values());
        priorityBox.setValue(Priority.MEDIUM);
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public void setTask(Task task) {
        this.taskToEdit = task;

        titleField.setText(task.getTitle());
        descriptionField.setText(task.getDescription());
        deadlineField.setText(task.getDeadline());
        priorityBox.setValue(task.getPriority());
    }

    @FXML
    private void handleCreate() {
        String title = titleField.getText().trim();

        if (title.isEmpty()) {
            errorLabel.setText("Le titre est obligatoire");
            return;
        }

        try {
            if (taskToEdit == null) {
                // CREATE
                taskService.createTask(
                        title,
                        descriptionField.getText(),
                        Status.TODO,
                        priorityBox.getValue(),
                        deadlineField.getText().isEmpty() ? null : deadlineField.getText(),
                        null,
                        null,
                        project.getId(),
                        null
                );
            } else {
                // UPDATE
                taskToEdit.setTitle(title);
                taskToEdit.setDescription(descriptionField.getText());
                taskToEdit.setDeadline(deadlineField.getText());
                taskToEdit.setPriority(priorityBox.getValue());

                taskService.updateTask(taskToEdit); //
            }

            ((Stage) titleField.getScene().getWindow()).close();

        } catch (Exception e) {
            errorLabel.setText(e.getMessage());
        }
    }
}
