package org.example.ui.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.example.config.SessionManager;
import org.example.model.Project;
import org.example.model.Task;
import org.example.model.User;
import org.example.model.UserStory;
import org.example.model.enums.Status;
import org.example.repository.ProjectRepository;
import org.example.service.TaskService;
import org.example.service.UserStoryService;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KanbanController {

    @FXML private ListView<Task> todoList;
    @FXML private ListView<Task> inProgressList;
    @FXML private ListView<Task> doneList;

    @FXML private VBox todoColumn;
    @FXML private VBox inProgressColumn;
    @FXML private VBox doneColumn;

    @FXML private ComboBox<String> filterPriorityBox;
    @FXML private ComboBox<String> sortBox;
    @FXML private ComboBox<String> filterUserStoryBox;
    @FXML private ToggleButton myTasksBtn;

    private final TaskService taskService = new TaskService();
    private final UserStoryService userStoryService = new UserStoryService();
    private final ProjectRepository projectRepository = new ProjectRepository();
    private Project project;
    private List<UserStory> projectUserStories;
    private Map<Long, String> userStoryNames = new HashMap<>();
    private Map<Long, String> memberNames = new HashMap<>();

    public void setProject(Project project) {
        this.project = project;
        loadUserStories();
        loadMemberNames();
        loadTasks();
    }

    private void loadMemberNames() {
        memberNames.clear();
        for (User u : projectRepository.findMembers(project.getId())) {
            memberNames.put(u.getId(), u.getFirstName() + " " + u.getLastName());
        }
    }

    private void loadUserStories() {
        projectUserStories = userStoryService.getUserStoriesByProject(project.getId());
        userStoryNames.clear();
        for (UserStory us : projectUserStories) {
            userStoryNames.put(us.getId(), us.getTitle());
        }

        String previousSelection = filterUserStoryBox.getValue();
        filterUserStoryBox.getItems().clear();
        filterUserStoryBox.getItems().add("Toutes");
        for (UserStory us : projectUserStories) {
            filterUserStoryBox.getItems().add(us.getTitle());
        }
        filterUserStoryBox.getItems().add("Sans User Story");

        if (previousSelection != null && filterUserStoryBox.getItems().contains(previousSelection)) {
            filterUserStoryBox.setValue(previousSelection);
        } else {
            filterUserStoryBox.setValue("Toutes");
        }
    }

    @FXML
    public void initialize() {

        setupCellFactory(todoList);
        setupCellFactory(inProgressList);
        setupCellFactory(doneList);

        setupDropTarget(todoList, Status.TODO);
        setupDropTarget(inProgressList, Status.IN_PROGRESS);
        setupDropTarget(doneList, Status.DONE);

        // FILTRE PRIORITÉ
        filterPriorityBox.getItems().addAll("Toutes", "Faibles", "Moyennes", "Elevées", "Critiques");
        filterPriorityBox.setValue("Toutes");
        filterPriorityBox.setOnAction(e -> loadTasks());

        // FILTRE USER STORY
        filterUserStoryBox.setValue("Toutes");
        filterUserStoryBox.setOnAction(e -> loadTasks());

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


    private void setupCellFactory(ListView<Task> listView) {

        listView.setCellFactory(lv -> {

            ListCell<Task> cell = new ListCell<>() {

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
                    title.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e293b;");

                    // PRIORITÉ
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

                    // ASSIGNÉ + CHEF DE PROJET
                    String assigneeName = task.getAssignedUserId() != null
                            ? memberNames.getOrDefault(task.getAssignedUserId(), null) : null;
                    String leaderName = task.getTaskLeaderId() != null
                            ? memberNames.getOrDefault(task.getTaskLeaderId(), null) : null;

                    HBox peopleRow = new HBox(8);
                    if (assigneeName != null) {
                        Label al = new Label("\uD83D\uDC64 " + assigneeName);
                        al.setStyle("-fx-text-fill: #374151; -fx-font-size: 11px;");
                        peopleRow.getChildren().add(al);
                    }
                    if (leaderName != null) {
                        Label ll = new Label("\uD83C\uDFAF " + leaderName);
                        ll.setStyle("-fx-text-fill: #374151; -fx-font-size: 11px;");
                        peopleRow.getChildren().add(ll);
                    }

                    // USER STORY LABEL
                    VBox card;
                    if (task.getUserStoryId() != null && userStoryNames.containsKey(task.getUserStoryId())) {
                        Label usLabel = new Label("\uD83D\uDCD6 " + userStoryNames.get(task.getUserStoryId()));
                        usLabel.setStyle("-fx-text-fill: #7c3aed; -fx-font-size: 11px; -fx-font-weight: bold;");

                        // DEADLINE
                        Label deadline = new Label(
                                task.getDeadline() != null ? "Deadline " + task.getDeadline() : ""
                        );
                        deadline.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px;");

                        card = new VBox(topRow, usLabel, deadline, peopleRow);
                    } else {
                        // DEADLINE
                        Label deadline = new Label(
                                task.getDeadline() != null ? "Deadline " + task.getDeadline() : ""
                        );
                        deadline.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px;");

                        card = new VBox(topRow, deadline, peopleRow);
                    }

                    card.setSpacing(5);
                    card.setStyle("-fx-padding: 10; -fx-background-radius: 10;");

                    card.setMouseTransparent(true);

                    switch (task.getPriority()) {
                        case LOW      -> card.setStyle(card.getStyle() + "-fx-background-color: #f0fdf4;");
                        case MEDIUM   -> card.setStyle(card.getStyle() + "-fx-background-color: #eff6ff;");
                        case HIGH     -> card.setStyle(card.getStyle() + "-fx-background-color: #fefce8;");
                        case CRITICAL -> card.setStyle(card.getStyle() + "-fx-background-color: #fef2f2;");
                    }

                    setGraphic(card);
                    setStyle("-fx-padding: 5;");
                }
            };

            cell.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && cell.getItem() != null) {
                    openEditTask(cell.getItem());
                }
            });

            cell.setOnDragDetected(event -> {
                if (cell.isEmpty()) return;

                Dragboard db = cell.startDragAndDrop(TransferMode.MOVE);

                ClipboardContent content = new ClipboardContent();
                content.putString(String.valueOf(cell.getItem().getId()));

                db.setContent(content);

                event.consume();
            });

            return cell;
        });
    }

    private void setupDropTarget(ListView<Task> listView, Status targetStatus) {

        listView.setOnDragOver(event -> {
            if (event.getGestureSource() != listView &&
                    event.getDragboard().hasString()) {

                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        listView.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();

            if (db.hasString()) {
                Long taskId = Long.parseLong(db.getString());

                taskService.updateTaskStatus(taskId, targetStatus);
                loadTasks();

                event.setDropCompleted(true);
            } else {
                event.setDropCompleted(false);
            }

            event.consume();
        });
    }


    @FXML
    private void loadTasks() {

        todoList.getItems().clear();
        inProgressList.getItems().clear();
        doneList.getItems().clear();

        List<Task> tasks = taskService.getTasksByProject(project.getId());

        // FILTRE "MES TÂCHES"
        if (myTasksBtn != null && myTasksBtn.isSelected()) {
            Long currentUserId = SessionManager.getCurrentUser().getId();
            tasks = tasks.stream()
                    .filter(t -> currentUserId.equals(t.getAssignedUserId()))
                    .toList();
        }

        String selectedPriority = filterPriorityBox.getValue();
        String selectedUserStory = filterUserStoryBox.getValue();
        String sort = sortBox.getValue();

        // FILTRE PRIORITÉ
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

        // FILTRE USER STORY
        if (selectedUserStory != null && !selectedUserStory.equals("Toutes")) {
            if (selectedUserStory.equals("Sans User Story")) {
                tasks = tasks.stream()
                        .filter(t -> t.getUserStoryId() == null)
                        .toList();
            } else {
                Long usId = null;
                for (UserStory us : projectUserStories) {
                    if (us.getTitle().equals(selectedUserStory)) {
                        usId = us.getId();
                        break;
                    }
                }
                Long finalUsId = usId;
                if (finalUsId != null) {
                    tasks = tasks.stream()
                            .filter(t -> finalUsId.equals(t.getUserStoryId()))
                            .toList();
                }
            }
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


    @FXML
    private void handleAddTask() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/createTask.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.focusedProperty().addListener((obs, wasFocused, focused) -> {
                if (!focused) stage.close();
            });

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
            stage.focusedProperty().addListener((obs, wasFocused, focused) -> {
                if (!focused) stage.close();
            });

            CreateTaskController controller = loader.getController();
            controller.setProject(project);
            controller.setTask(task);

            stage.setTitle("Modifier la tâche");
            stage.showAndWait();

            loadTasks();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleOpenUserStories() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/userStory.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("User Stories - " + project.getName());

            UserStoryController controller = loader.getController();
            controller.setProject(project);

            stage.showAndWait();
            loadUserStories();
            loadTasks();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleOpenMembers() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/members.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load(), 700, 500));
            stage.setTitle("Membres - " + project.getName());

            MembersController controller = loader.getController();
            controller.setProject(project);

            stage.showAndWait();
            loadMemberNames();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
