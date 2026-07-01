package org.example.ui.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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
import org.example.ui.util.Modals;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class KanbanController {

    private static final Logger LOGGER = Logger.getLogger(KanbanController.class.getName());

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
    @FXML private TextField searchField;
    @FXML private Label todoCountLabel;
    @FXML private Label inProgressCountLabel;
    @FXML private Label doneCountLabel;

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

        setupColumnRightClick(todoList);
        setupColumnRightClick(inProgressList);
        setupColumnRightClick(doneList);

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

        // RECHERCHE
        searchField.textProperty().addListener((obs, oldText, newText) -> loadTasks());
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
                    title.getStyleClass().add("task-title");

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
                        al.getStyleClass().add("task-meta");
                        peopleRow.getChildren().add(al);
                    }
                    if (leaderName != null) {
                        Label ll = new Label("\uD83C\uDFAF " + leaderName);
                        ll.getStyleClass().add("task-meta");
                        peopleRow.getChildren().add(ll);
                    }

                    // DEADLINE
                    Label deadline = new Label(
                            task.getDeadline() != null ? "Deadline " + task.getDeadline() : ""
                    );
                    deadline.getStyleClass().add("task-meta");

                    // USER STORY LABEL
                    VBox card;
                    if (task.getUserStoryId() != null && userStoryNames.containsKey(task.getUserStoryId())) {
                        Label usLabel = new Label("\uD83D\uDCD6 " + userStoryNames.get(task.getUserStoryId()));
                        usLabel.getStyleClass().add("task-us-label");
                        card = new VBox(topRow, usLabel, deadline, peopleRow);
                    } else {
                        card = new VBox(topRow, deadline, peopleRow);
                    }

                    card.setSpacing(5);
                    card.setMouseTransparent(true);
                    card.getStyleClass().add("task-card");
                    card.getStyleClass().add(switch (task.getPriority()) {
                        case LOW      -> "task-card-low";
                        case MEDIUM   -> "task-card-medium";
                        case HIGH     -> "task-card-high";
                        case CRITICAL -> "task-card-critical";
                    });

                    setGraphic(card);
                    setStyle("-fx-padding: 5;");
                }
            };

            cell.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.SECONDARY) {
                    if (!cell.isEmpty() && cell.getItem() != null) {
                        buildTaskContextMenu(cell.getItem()).show(cell, event.getScreenX(), event.getScreenY());
                    } else {
                        buildColumnContextMenu().show(cell, event.getScreenX(), event.getScreenY());
                    }
                    event.consume();
                } else if (event.getClickCount() == 2 && !cell.isEmpty() && cell.getItem() != null) {
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

        // FILTRE RECHERCHE
        String query = searchField.getText() != null ? searchField.getText().trim().toLowerCase() : "";
        if (!query.isEmpty()) {
            tasks = tasks.stream()
                    .filter(t -> (t.getTitle() != null && t.getTitle().toLowerCase().contains(query))
                            || (t.getDescription() != null && t.getDescription().toLowerCase().contains(query)))
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

        todoCountLabel.setText(String.valueOf(todoList.getItems().size()));
        inProgressCountLabel.setText(String.valueOf(inProgressList.getItems().size()));
        doneCountLabel.setText(String.valueOf(doneList.getItems().size()));
    }


    @FXML
    private void handleAddTask() {
        CreateTaskController controller = Modals.open(todoList, "/fxml/createTask.fxml",
                540, 700, this::loadTasks);
        controller.setProject(project);
    }

    @FXML
    private void goBackToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
            Stage stage = (Stage) todoList.getScene().getWindow();
            stage.getScene().setRoot(loader.load());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error", e);
        }
    }

    @FXML
    private void openEditTask(Task task) {
        CreateTaskController controller = Modals.open(todoList, "/fxml/createTask.fxml",
                540, 700, this::loadTasks);
        controller.setProject(project);
        controller.setTask(task);
    }

    @FXML
    private void handleOpenUserStories() {
        UserStoryController controller = Modals.open(todoList, "/fxml/userStory.fxml",
                760, 560, () -> {
                    loadUserStories();
                    loadTasks();
                });
        controller.setProject(project);
    }

    /* ------------------------------------------------------------------ */
    /*  Menus contextuels                                                 */
    /* ------------------------------------------------------------------ */

    /** Clic droit sur la zone vide d'une colonne (pas sur une tâche). */
    private void setupColumnRightClick(ListView<Task> listView) {
        listView.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                buildColumnContextMenu().show(listView, event.getScreenX(), event.getScreenY());
                event.consume();
            }
        });
    }

    private ContextMenu buildColumnContextMenu() {
        MenuItem addTask = new MenuItem("＋ Ajouter une tâche");
        addTask.setOnAction(e -> handleAddTask());

        MenuItem addUserStory = new MenuItem("＋ Ajouter une User Story");
        addUserStory.setOnAction(e -> handleOpenUserStories());

        MenuItem addMember = new MenuItem("＋ Ajouter un membre");
        addMember.setOnAction(e -> handleOpenMembers());

        return new ContextMenu(addTask, addUserStory, addMember);
    }

    private ContextMenu buildTaskContextMenu(Task task) {
        // Changement de statut — flèche selon la direction
        int cur = task.getStatus().ordinal();

        MenuItem toTodo = new MenuItem((cur > Status.TODO.ordinal() ? "← " : "→ ") + "À faire");
        toTodo.setDisable(task.getStatus() == Status.TODO);
        toTodo.setOnAction(e -> { taskService.updateTaskStatus(task.getId(), Status.TODO); loadTasks(); });

        MenuItem toInProgress = new MenuItem((cur > Status.IN_PROGRESS.ordinal() ? "← " : "→ ") + "En cours");
        toInProgress.setDisable(task.getStatus() == Status.IN_PROGRESS);
        toInProgress.setOnAction(e -> { taskService.updateTaskStatus(task.getId(), Status.IN_PROGRESS); loadTasks(); });

        MenuItem toDone = new MenuItem((cur > Status.DONE.ordinal() ? "← " : "→ ") + "Terminé");
        toDone.setDisable(task.getStatus() == Status.DONE);
        toDone.setOnAction(e -> { taskService.updateTaskStatus(task.getId(), Status.DONE); loadTasks(); });

        MenuItem edit = new MenuItem("Modifier");
        edit.setOnAction(e -> openEditTask(task));

        MenuItem delete = new MenuItem("Supprimer");
        delete.setStyle("-fx-text-fill: #dc2626;");
        delete.setOnAction(e -> handleDeleteTask(task));

        return new ContextMenu(toTodo, toInProgress, toDone, new SeparatorMenuItem(), edit, delete);
    }

    private void handleDeleteTask(Task task) {
        Modals.confirmDelete(todoList,
                "Supprimer la tâche \"" + task.getTitle() + "\" ?",
                () -> {
                    taskService.deleteTask(task.getId());
                    loadTasks();
                });
    }

    @FXML
    private void handleOpenMembers() {
        MembersController controller = Modals.open(todoList, "/fxml/members.fxml",
                940, 600, this::loadMemberNames);
        controller.setProject(project);
    }

}
