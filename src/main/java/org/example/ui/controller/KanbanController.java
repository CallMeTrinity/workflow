package org.example.ui.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.model.Project;
import org.example.model.Task;
import org.example.model.enums.Status;
import org.example.service.TaskService;

import java.util.List;

public class KanbanController {

    @FXML private ListView<Task> todoList;
    @FXML private ListView<Task> inProgressList;
    @FXML private ListView<Task> doneList;

    @FXML private VBox todoColumn;
    @FXML private VBox inProgressColumn;
    @FXML private VBox doneColumn;

    private final TaskService taskService = new TaskService();
    private Project project;

    public void setProject(Project project) {
        this.project = project;
        loadTasks();
    }

    @FXML
    public void initialize() {
        setupCellFactory(todoList);
        setupCellFactory(inProgressList);
        setupCellFactory(doneList);

        setupDropTarget(todoColumn, Status.TODO);
        setupDropTarget(inProgressColumn, Status.IN_PROGRESS);
        setupDropTarget(doneColumn, Status.DONE);
    }

    private void setupCellFactory(ListView<Task> listView) {
        listView.setCellFactory(lv -> {
            ListCell<Task> cell = new ListCell<>() {
                @Override
                protected void updateItem(Task task, boolean empty) {
                    super.updateItem(task, empty);
                    setText(empty || task == null ? null : task.getTitle());
                }
            };

            cell.setOnDragDetected(event -> {
                Task task = cell.getItem();
                if (task == null) return;

                Dragboard db = cell.startDragAndDrop(TransferMode.COPY_OR_MOVE);
                ClipboardContent content = new ClipboardContent();
                content.putString(String.valueOf(task.getId()));
                db.setContent(content);
                event.consume();
            });

            return cell;
        });
    }

    private void setupDropTarget(VBox column, Status targetStatus) {
        // Event filters intercept during CAPTURE phase, before children see the events
        column.addEventFilter(DragEvent.DRAG_OVER, event -> {
            if (event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                event.consume();
            }
        });

        column.addEventFilter(DragEvent.DRAG_DROPPED, event -> {
            Dragboard db = event.getDragboard();
            if (db.hasString()) {
                try {
                    Long taskId = Long.parseLong(db.getString());
                    taskService.updateTaskStatus(taskId, targetStatus);
                    loadTasks();
                    event.setDropCompleted(true);
                } catch (Exception e) {
                    e.printStackTrace();
                    event.setDropCompleted(false);
                }
            } else {
                event.setDropCompleted(false);
            }
            event.consume();
        });

        column.setOnDragEntered(event -> {
            if (event.getDragboard().hasString()) {
                column.setStyle("-fx-border-color: #2196F3; -fx-border-width: 2; -fx-border-radius: 5;");
            }
        });

        column.setOnDragExited(event -> {
            column.setStyle("");
        });
    }

    private void loadTasks() {
        todoList.getItems().clear();
        inProgressList.getItems().clear();
        doneList.getItems().clear();

        List<Task> tasks = taskService.getTasksByProject(project.getId());
        for (Task task : tasks) {
            switch (task.getStatus()) {
                case TODO        -> todoList.getItems().add(task);
                case IN_PROGRESS -> inProgressList.getItems().add(task);
                case DONE        -> doneList.getItems().add(task);
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
