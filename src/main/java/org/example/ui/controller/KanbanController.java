package org.example.ui.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.example.model.Project;
import org.example.model.Task;
import org.example.model.enums.Status;
import org.example.service.TaskService;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

public class KanbanController {

    @FXML private ListView<Task> todoList;
    @FXML private ListView<Task> inProgressList;
    @FXML private ListView<Task> doneList;

    @FXML private VBox todoColumn;
    @FXML private VBox inProgressColumn;
    @FXML private VBox doneColumn;

    @FXML private ComboBox<String> filterPriorityBox;
    @FXML private ComboBox<String> sortBox;

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

        // FILTRE
        filterPriorityBox.getItems().addAll("Toutes", "Faibles", "Moyennes", "Elevées", "Critiques");
        filterPriorityBox.setValue("Toutes");
        filterPriorityBox.setOnAction(e -> loadTasks());

        // TRI
        sortBox.getItems().addAll(
                "Aucun",
                "Priorité ↑",
                "Priorité ↓",
                "Deadline ↑",
                "Deadline ↓"
        );
        sortBox.setValue("Aucun");
        sortBox.setOnAction(e -> loadTasks());
    }

    // ========================= UI CARDS =========================

    private void setupCellFactory(ListView<Task> listView) {

        listView.setCellFactory(lv -> new ListCell<>() {

            @Override
            protected void updateItem(Task task, boolean empty) {
                super.updateItem(task, empty);

                if (empty || task == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                // TITRE
                Label title = new Label(task.getTitle());
                title.setStyle("-fx-font-weight: bold;");

                // PRIORITÉ (étoiles)
                String priorityVisual = switch (task.getPriority()) {
                    case LOW -> "☆";
                    case MEDIUM -> "☆☆";
                    case HIGH -> "☆☆☆";
                    case CRITICAL -> "⚠";
                };

                Label priority = new Label(priorityVisual);

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                HBox topRow = new HBox(title, spacer, priority);

                // DEADLINE
                Label deadline = new Label(
                        task.getDeadline() != null ? "Deadline " + task.getDeadline() : ""
                );

                // CARD
                VBox card = new VBox(topRow, deadline);
                card.setSpacing(5);
                card.setStyle("""
                    -fx-padding: 10;
                    -fx-background-radius: 10;
                """);

                // COULEURS PASTEL
                switch (task.getPriority()) {
                    case LOW -> card.setStyle(card.getStyle() + "-fx-background-color: #eeeeee;");
                    case MEDIUM -> card.setStyle(card.getStyle() + "-fx-background-color: #bbdefb;");
                    case HIGH -> card.setStyle(card.getStyle() + "-fx-background-color: #ffe0b2;");
                    case CRITICAL -> card.setStyle(card.getStyle() + "-fx-background-color: #ffcdd2;");
                }

                setGraphic(card);

                setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2 && getItem() != null) {
                        openEditTask(getItem());
                    }
                });

                setOnDragDetected(event -> {
                    if (getItem() == null) return;

                    Dragboard db = startDragAndDrop(TransferMode.MOVE);
                    ClipboardContent content = new ClipboardContent();

                    content.putString(String.valueOf(getItem().getId()));
                    db.setContent(content);

                    event.consume();
                });

                // ESPACE ENTRE CARTES
                setStyle("-fx-padding: 5;");
            }
        });
    }

    // ========================= DRAG & DROP =========================

    private void setupDropTarget(VBox column, Status targetStatus) {

        column.addEventFilter(DragEvent.DRAG_OVER, event -> {
            if (event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
                event.consume();
            }
        });

        column.addEventFilter(DragEvent.DRAG_DROPPED, event -> {
            Dragboard db = event.getDragboard();

            if (db.hasString()) {
                Long taskId = Long.parseLong(db.getString());
                taskService.updateTaskStatus(taskId, targetStatus);
                loadTasks();
                event.setDropCompleted(true);
            }

            event.consume();
        });
    }

    // ========================= LOAD + FILTER + SORT =========================

    private void loadTasks() {

        todoList.getItems().clear();
        inProgressList.getItems().clear();
        doneList.getItems().clear();

        List<Task> tasks = taskService.getTasksByProject(project.getId());

        String selectedPriority = filterPriorityBox.getValue();
        String sort = sortBox.getValue();

        // FILTRE
        if (!selectedPriority.equals("Toutes")) {

            String mappedPriority = switch (selectedPriority) {
                case "Faibles" -> "LOW";
                case "Moyennes" -> "MEDIUM";
                case "Elevées" -> "HIGH";
                case "Critiques" -> "CRITICAL";
                default -> "";
            };

            tasks = tasks.stream()
                    .filter(t -> t.getPriority().name().equals(mappedPriority))
                    .toList();
        }

        // TRI
        Comparator<Task> comparator = null;

        switch (sort) {
            case "Priorité ↑" ->
                    comparator = Comparator.comparing(Task::getPriority);

            case "Priorité ↓" ->
                    comparator = Comparator.comparing(Task::getPriority).reversed();

            case "Deadline ↑" ->
                    comparator = Comparator.comparing(t ->
                            t.getDeadline() != null ? LocalDate.parse(t.getDeadline()) : LocalDate.MAX
                    );

            case "Deadline ↓" ->
                    comparator = Comparator.comparing(
                            (Task t) -> t.getDeadline() != null ? LocalDate.parse(t.getDeadline()) : LocalDate.MIN
                    ).reversed();
        }

        if (comparator != null) {
            tasks = tasks.stream().sorted(comparator).toList();
        }

        // RÉPARTITION
        for (Task task : tasks) {
            switch (task.getStatus()) {
                case TODO -> todoList.getItems().add(task);
                case IN_PROGRESS -> inProgressList.getItems().add(task);
                case DONE -> doneList.getItems().add(task);
            }
        }
    }

    // ========================= ACTIONS =========================

    @FXML
    private void handleAddTask() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/createTask.fxml"));
            Stage stage = new Stage();
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openEditTask(Task task) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/createTask.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));

            CreateTaskController controller = loader.getController();
            controller.setProject(project);
            controller.setTask(task);

            stage.setTitle("Modifier la tâche");
            stage.showAndWait();

            loadTasks(); // refresh

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}