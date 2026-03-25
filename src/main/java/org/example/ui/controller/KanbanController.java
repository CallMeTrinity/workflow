package org.example.ui.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import org.example.model.Project;
import org.example.model.Task;
import org.example.model.enums.Status;
import org.example.service.TaskService;

import java.util.List;

public class KanbanController {

    @FXML private ListView<String> todoList;
    @FXML private ListView<String> inProgressList;
    @FXML private ListView<String> doneList;

    private final TaskService taskService = new TaskService();
    private Project project;

    public void setProject(Project project) {
        this.project = project;
        loadTasks();
    }

    private void loadTasks() {
        todoList.getItems().clear();
        inProgressList.getItems().clear();
        doneList.getItems().clear();

        List<Task> tasks = taskService.getTasksByProject(project.getId());
        for (Task task : tasks) {
            if (task.getStatus() == Status.TODO) {
                todoList.getItems().add(task.getTitle());
            } else if (task.getStatus() == Status.IN_PROGRESS) {
                inProgressList.getItems().add(task.getTitle());
            } else if (task.getStatus() == Status.DONE) {
                doneList.getItems().add(task.getTitle());
            }
        }
    }

    @FXML
    private void goBackToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
            Stage stage = (Stage) todoList.getScene().getWindow();
            Scene scene = stage.getScene();
            scene.setRoot(loader.load());
            stage.setTitle("Workflow - Dashboard");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
