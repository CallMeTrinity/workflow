package org.example.ui.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.stage.Stage;
import org.example.model.Project;
import org.example.model.Task;
import org.example.model.enums.Priority;
import org.example.model.enums.Status;
import org.example.service.TaskService;

import java.util.List;

public class KanbanController {

    @FXML private ListView<Task> todoList;
    @FXML private ListView<Task> inProgressList;
    @FXML private ListView<Task> doneList;

    private final TaskService taskService = new TaskService();
    private Project project;

    public void setProject(Project project) {
        this.project = project;
        setupDragAndDrop();
        loadTasks();
    }

    @FXML
    public void initialize() {
        // Affiche le titre de la tâche dans chaque cellule
        setupCellFactory(todoList);
        setupCellFactory(inProgressList);
        setupCellFactory(doneList);
    }

    private void setupCellFactory(ListView<Task> listView) {
        listView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Task task, boolean empty) {
                super.updateItem(task, empty);
                setText(empty || task == null ? null : task.getTitle());
            }
        });
    }

    private void setupDragAndDrop() {
        setupDragSource(todoList);
        setupDragSource(inProgressList);
        setupDragSource(doneList);

        setupDropTarget(todoList, Status.TODO);
        setupDropTarget(inProgressList, Status.IN_PROGRESS);
        setupDropTarget(doneList, Status.DONE);
    }

    private void setupDragSource(ListView<Task> listView) {
        listView.setOnDragDetected(event -> {
            Task selected = listView.getSelectionModel().getSelectedItem();
            if (selected == null) return;

            Dragboard db = listView.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(String.valueOf(selected.getId()));
            db.setContent(content);
            event.consume();
        });
    }

    private void setupDropTarget(ListView<Task> listView, Status targetStatus) {
        listView.setOnDragOver(event -> {
            if (event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        listView.setOnDragDropped(event -> {
            String idStr = event.getDragboard().getString();
            Long taskId = Long.parseLong(idStr);

            taskService.updateTaskStatus(taskId, targetStatus);
            loadTasks();

            event.setDropCompleted(true);
            event.consume();
        });
    }

    private void loadTasks() {
        todoList.getItems().clear();
        inProgressList.getItems().clear();
        doneList.getItems().clear();

        List<Task> tasks = taskService.getTasksByProject(project.getId());
        for (Task task : tasks) {
            switch (task.getStatus()) {
                case TODO       -> todoList.getItems().add(task);
                case IN_PROGRESS -> inProgressList.getItems().add(task);
                case DONE       -> doneList.getItems().add(task);
            }
        }
    }

    @FXML
    private void handleAddTask() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/createTask.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Créer une tâche");
            stage.setScene(new Scene(loader.load()));

            CreateTaskController controller = loader.getController();
            controller.setProject(project);

            stage.showAndWait();
            loadTasks();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goBackToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
            Stage stage = (Stage) todoList.getScene().getWindow();
            stage.getScene().setRoot(loader.load());
            stage.setTitle("Workflow - Dashboard");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
