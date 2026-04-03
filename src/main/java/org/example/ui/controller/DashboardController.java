package org.example.ui.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import org.example.config.SessionManager;
import org.example.model.Project;
import org.example.service.AuthService;
import org.example.service.NotificationService;
import org.example.service.ProjectService;
import org.example.service.UserService;

import java.util.List;

public class DashboardController {

    @FXML private ListView<String> projectList;
    @FXML private Label welcomeLabel;

    private final ProjectService projectService = new ProjectService();
    private final AuthService authService = new AuthService();
    private List<Project> projects;
    private final UserService userService = new UserService();
    private final NotificationService notificationService = new NotificationService();

    @FXML
    public void initialize() {
        // Affiche le nom de l'utilisateur connecté
        if (SessionManager.getCurrentUser() != null) {
            welcomeLabel.setText("Bonjour, " + SessionManager.getCurrentUser().getFullName());
        }
        refreshProjects();

        projectList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                openKanban();
            }
        });
    }

    private void refreshProjects() {
        projectList.getItems().clear();
        projects = projectService.getAllProjects();
        for (Project project : projects) {
            projectList.getItems().add(project.getName());
        }
    }

    @FXML
    private void openCreateProject() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/createProject.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Créer un projet");
            stage.setScene(new Scene(loader.load()));
            stage.showAndWait();
            refreshProjects();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openKanban() {
        // Récupère le projet sélectionné
        int selectedIndex = projectList.getSelectionModel().getSelectedIndex();
        if (selectedIndex < 0) return; // rien de sélectionné

        Project selectedProject = projects.get(selectedIndex);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/kanban.fxml"));



            Stage stage = (Stage) projectList.getScene().getWindow();
            Scene scene = stage.getScene();
            scene.setRoot(loader.load());
            stage.setTitle("Kanban - " + selectedProject.getName());
            KanbanController controller = loader.getController();
            controller.setProject(selectedProject);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        authService.logout();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Stage stage = (Stage) projectList.getScene().getWindow();
            Scene scene = stage.getScene();
            scene.setRoot(loader.load());
            stage.setTitle("Workflow");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openRooms() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/room.fxml"));
            Stage stage = (Stage) projectList.getScene().getWindow();
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
            Stage stage = (Stage) projectList.getScene().getWindow();
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
            Stage stage = (Stage) projectList.getScene().getWindow();
            stage.getScene().setRoot(loader.load());
            stage.setTitle("Workflow - Planning");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void openProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ProfileView.fxml"));
            Parent root = loader.load();

            ProfilController controller = loader.getController();
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
