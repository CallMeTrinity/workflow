package org.example.ui.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.config.SessionManager;
import org.example.model.Project;
import org.example.service.AuthService;
import org.example.service.ProjectService;

import java.util.List;

public class DashboardController {

    @FXML private TableView<Project> projectTable;
    @FXML private TableColumn<Project, String> nameColumn;
    @FXML private TableColumn<Project, String> descriptionColumn;
    @FXML private TableColumn<Project, String> startDateColumn;
    @FXML private TableColumn<Project, String> endDateColumn;
    @FXML private TableColumn<Project, String> createdAtColumn;
    @FXML private Label welcomeLabel;

    private final ProjectService projectService = new ProjectService();
    private final AuthService authService = new AuthService();

    @FXML
    public void initialize() {
        if (SessionManager.getCurrentUser() != null) {
            welcomeLabel.setText("Bonjour, " + SessionManager.getCurrentUser().getFullName());
        }

        nameColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getName()));
        descriptionColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getDescription()));
        startDateColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getStartDate()));
        endDateColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getEndDate()));
        createdAtColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getCreatedAt()));

        // Double clic → kanban
        projectTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                openKanban();
            }
        });

        refreshProjects();
    }

    private void refreshProjects() {
        projectTable.getItems().clear();
        projectTable.getItems().addAll(projectService.getAllProjects());
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
    private void openEditProject() {
        Project selected = projectTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/editProject.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Modifier le projet");
            stage.setScene(new Scene(loader.load()));

            EditProjectController controller = loader.getController();
            controller.setProject(selected);

            stage.showAndWait();
            refreshProjects();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void deleteProject() {
        Project selected = projectTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer le projet \"" + selected.getName() + "\" ?",
                ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                projectService.deleteProject(selected.getId());
                refreshProjects();
            }
        });
    }

    @FXML
    private void openKanban() {
        Project selected = projectTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/kanban.fxml"));
            Stage stage = (Stage) projectTable.getScene().getWindow();
            stage.getScene().setRoot(loader.load());
            stage.setTitle("Kanban - " + selected.getName());
            KanbanController controller = loader.getController();
            controller.setProject(selected);
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
}
