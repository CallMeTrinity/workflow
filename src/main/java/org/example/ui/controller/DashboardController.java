package org.example.ui.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import org.example.config.SessionManager;
import org.example.model.Project;
import org.example.model.User;
import org.example.model.enums.Role;
import org.example.repository.UserRepository;
import org.example.service.AuthService;
import org.example.service.NotificationService;
import org.example.service.ProjectService;
import org.example.service.UserService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DashboardController {

    @FXML private TableView<Project> projectTable;
    @FXML private TableColumn<Project, String> nameColumn;
    @FXML private TableColumn<Project, String> descriptionColumn;
    @FXML private TableColumn<Project, String> leaderColumn;
    @FXML private TableColumn<Project, String> startDateColumn;
    @FXML private TableColumn<Project, String> endDateColumn;
    @FXML private TableColumn<Project, String> createdAtColumn;
    @FXML private TableColumn<Project, Void> actionsColumn;
    @FXML private Label welcomeLabel;
    @FXML private Button createProjectBtn;

    private final ProjectService projectService = new ProjectService();
    private final AuthService authService = new AuthService();
    private final UserRepository userRepository = new UserRepository();
    private List<Project> projects;
    private Map<Long, String> userNames;
    private final UserService userService = new UserService();
    private final NotificationService notificationService = new NotificationService();

    /** Le menu actuellement affiché — permet de n'en avoir qu'un seul ouvert. */
    private ContextMenu activeMenu;

    private Stage createProjectStage;
    private Stage editProjectStage;

    @FXML
    public void initialize() {
        if (SessionManager.getCurrentUser() != null) {
            welcomeLabel.setText("Bonjour, " + SessionManager.getCurrentUser().getFullName());
        }

        userNames = userRepository.findAll().stream()
                .collect(Collectors.toMap(User::getId, User::getFullName));

        nameColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getName()));
        descriptionColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getDescription()));
        leaderColumn.setCellValueFactory(data ->
                new SimpleStringProperty(userNames.getOrDefault(data.getValue().getProjectLeaderId(), "—")));
        startDateColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getStartDate()));
        endDateColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getEndDate()));
        createdAtColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getCreatedAt()));

        setupActionsColumn();
        setupRowContextMenu();

        updateButtonVisibility();

        refreshProjects();
    }

    private void updateButtonVisibility(){
        boolean canCreate = isAdmin() || isProjectLeader();
        createProjectBtn.setVisible(canCreate);
        createProjectBtn.setManaged(canCreate);
    }

    /* ------------------------------------------------------------------ */
    /*  Colonne "⋮"                                                       */
    /* ------------------------------------------------------------------ */

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("⋮");

            {
                btn.setStyle("-fx-background-color: transparent; -fx-font-size: 16px; "
                        + "-fx-text-fill: #64748b; -fx-cursor: hand; -fx-padding: 0 6 0 6;");
                btn.setOnAction(e -> {
                    Project project = getTableView().getItems().get(getIndex());
                    showProjectMenu(project, btn, Side.BOTTOM);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
                setAlignment(Pos.CENTER);
            }
        });
    }

    /* ------------------------------------------------------------------ */
    /*  Clic droit sur une ligne                                          */
    /* ------------------------------------------------------------------ */

    private void setupRowContextMenu() {
        projectTable.setRowFactory(tv -> {
            TableRow<Project> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.SECONDARY && !row.isEmpty()) {
                    showProjectMenu(row.getItem(), row, event.getScreenX(), event.getScreenY());
                } else if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2 && !row.isEmpty()) {
                    openKanban(row.getItem());
                }
            });
            return row;
        });
    }

    /* ------------------------------------------------------------------ */
    /*  Construction + affichage du menu contextuel                        */
    /* ------------------------------------------------------------------ */

    private ContextMenu buildProjectMenu(Project project) {
        MenuItem openKanban = new MenuItem("Ouvrir Kanban");
        openKanban.setOnAction(e -> openKanban(project));

        ContextMenu menu = new ContextMenu(openKanban);
        // Ajouter "Supprimer" seulement si l'utilisateur est admin
        if (isAdmin() || isProjectLeader()) {
            MenuItem edit = new MenuItem("Modifier");
            edit.setOnAction(e -> openEditProject(project));
            menu.getItems().add(edit);
        }
        if (isAdmin()) {
            menu.getItems().add(new SeparatorMenuItem());
            MenuItem delete = new MenuItem("Supprimer");
            delete.setStyle("-fx-text-fill: #dc2626;");
            delete.setOnAction(e -> deleteProject(project));
            menu.getItems().add(delete);
        }

        return menu;
    }

    /** Affiche à côté d'un nœud (bouton ⋮). */
    private void showProjectMenu(Project project, javafx.scene.Node anchor, Side side) {
        closeActiveMenu();
        activeMenu = buildProjectMenu(project);
        activeMenu.show(anchor, side, 0, 0);
    }

    /** Affiche à une position écran (clic droit). */
    private void showProjectMenu(Project project, javafx.scene.Node anchor, double screenX, double screenY) {
        closeActiveMenu();
        activeMenu = buildProjectMenu(project);
        activeMenu.show(anchor, screenX, screenY);
    }

    private void closeActiveMenu() {
        if (activeMenu != null) {
            activeMenu.hide();
            activeMenu = null;
        }
    }

    /* ------------------------------------------------------------------ */
    /*  Actions projet                                                    */
    /* ------------------------------------------------------------------ */

    private void openKanban(Project project) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/kanban.fxml"));
            Stage stage = (Stage) projectTable.getScene().getWindow();
            stage.getScene().setRoot(loader.load());
            stage.setTitle("Kanban - " + project.getName());
            KanbanController controller = loader.getController();
            controller.setProject(project);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openEditProject(Project project) {
        if (editProjectStage != null && editProjectStage.isShowing()) editProjectStage.close();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/editProject.fxml"));
            editProjectStage = new Stage();
            editProjectStage.setTitle("Modifier le projet");
            editProjectStage.setScene(new Scene(loader.load()));
            editProjectStage.sizeToScene();
            editProjectStage.setOnHidden(e -> refreshProjects());

            EditProjectController controller = loader.getController();
            controller.setProject(project);

            editProjectStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteProject(Project project) {
        if (!isAdmin()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Vous n'avez pas les droits pour supprimer ce projet.");
            alert.showAndWait();
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer le projet \"" + project.getName() + "\" ?",
                ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                projectService.deleteProject(project.getId());
                refreshProjects();
            }
        });
    }

    /* ------------------------------------------------------------------ */
    /*  Refresh + navigation                                              */
    /* ------------------------------------------------------------------ */

    private void refreshProjects() {
        userNames = userRepository.findAll().stream()
                .collect(Collectors.toMap(User::getId, User::getFullName));
        projectTable.getItems().clear();
        projectTable.getItems().addAll(projectService.getAllProjects());
    }

    @FXML
    private void openCreateProject() {
        if (!isAdmin() && !isProjectLeader()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Vous n'avez pas les droits pour créer un projet.");
            alert.showAndWait();
            return;
        }
        if (createProjectStage != null && createProjectStage.isShowing()) createProjectStage.close();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/createProject.fxml"));
            createProjectStage = new Stage();
            createProjectStage.setTitle("Créer un projet");
            createProjectStage.setScene(new Scene(loader.load()));
            createProjectStage.sizeToScene();
            createProjectStage.setOnHidden(e -> refreshProjects());
            createProjectStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        authService.logout();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Stage stage = (Stage) projectTable.getScene().getWindow();
            stage.getScene().setRoot(loader.load());
            stage.setTitle("Workflow");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openRooms() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/room.fxml"));
            Stage stage = (Stage) projectTable.getScene().getWindow();
            stage.getScene().setRoot(loader.load());
            stage.setTitle("Workflow - Salles");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openReservations() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/reservation.fxml"));
            Stage stage = (Stage) projectTable.getScene().getWindow();
            stage.getScene().setRoot(loader.load());
            stage.setTitle("Workflow - Réservations");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openPlanning() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/planning.fxml"));
            Stage stage = (Stage) projectTable.getScene().getWindow();
            stage.getScene().setRoot(loader.load());
            stage.setTitle("Workflow - Planning");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // user roles

    private boolean isAdmin(){
        User currentUser = SessionManager.getCurrentUser();
        return currentUser.getRole() == Role.ADMIN;
    }

    private boolean isProjectLeader(){
        User currentUser = SessionManager.getCurrentUser();
        return currentUser.getRole() == Role.PROJECT_LEADER;
    }


    @FXML
    private void openProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/profile.fxml"));
            Parent root = loader.load();

            ProfileController controller = loader.getController();
            controller.init(SessionManager.getCurrentUser(), userService, notificationService);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML private Label avatarHeader;
    @FXML private Label notifBadge;

    public void updateHeader() {
        avatarHeader.setText(SessionManager.getCurrentUser().getUsername().substring(0,1).toUpperCase());

        int count = notificationService.countUnread(SessionManager.getCurrentUser().getId());

        notifBadge.setText(String.valueOf(count));
        notifBadge.setVisible(count > 0);
    }
}
